package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.ability.context.abilityInput
import cc.mewcraft.wakame.ability2.trigger.AbilityTrigger
import cc.mewcraft.wakame.ability2.trigger.AbilityTriggerVariant
import cc.mewcraft.wakame.ecs.bridge.KoishEntity
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.molang.Expression
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.KoishRegistries2
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.data.CompoundTag
import cc.mewcraft.wakame.util.data.getIntOrNull
import cc.mewcraft.wakame.util.data.getStringOrNull
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.text.mini
import cc.mewcraft.wakame.util.typeTokenOf
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import org.bukkit.entity.Player
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.function.Predicate

/**
 * 本函数用于从 NBT 构建 [PlayerAbility].
 *
 * 给定的 NBT 的结构必须是以下结构:
 *
 * ```NBT
 * string('trigger'): <trigger>
 * int('variant'): <variant>
 * string('mana_cost'): <manaCost>
 * ```
 *
 * @param id 技能的唯一标识, 也就是 [PlayerAbility.id]
 * @param tag 包含该技能数据的 NBT
 *
 * @return 从 NBT 构建的 [PlayerAbility]
 */
fun PlayerAbility(
    id: Key, tag: CompoundTag,
): PlayerAbility {
    val trigger = tag.readTrigger()
    val variant = tag.readVariant()
    val manaCost = tag.readExpression()
    return PlayerAbility(id, trigger, variant, manaCost)
}

/**
 * 本函数用于从配置文件构建 [PlayerAbility].
 *
 * 给定的 [ConfigurationNode] 必须是以下结构:
 *
 * ```yaml
 * <node>:
 *  trigger: <trigger>
 *  variant: <variant>
 *  mana_cost: <manaCost>
 * ```
 *
 * @param id 技能的唯一标识, 也就是 [PlayerAbility.id]
 * @param node 包含技能数据的配置节点
 *
 * @return 从配置文件构建的 [PlayerAbility]
 */
fun PlayerAbility(
    id: Key, node: ConfigurationNode,
): PlayerAbility {
    val trigger = node.node("trigger").get<AbilityTrigger>()
    val variant = node.node("variant").require<AbilityTriggerVariant>()
    val manaCost = node.node("mana_cost").require<Expression>()
    return PlayerAbility(id, trigger, variant, manaCost)
}

/**
 * 代表一个其他系统里的技能实例.
 *
 * 其他系统包括:
 * - 物品生成
 * - 技能设置
 *
 * 本数据类用于:
 * - 实现配置文件中技能的序列化
 * - 组成游戏内物品上的技能核心
 */
data class PlayerAbility(
    val id: Key,
    val trigger: AbilityTrigger?,
    val variant: AbilityTriggerVariant,
    val manaCost: Expression,
) {
    val instance: Ability
        get() = KoishRegistries.ABILITY.getOrThrow(id)
    val displayName: Component
        get() = instance.displays.name.mini
    val description: List<Component>
        get() = instance.displays.tooltips.mini

    fun record(caster: Player, target: KoishEntity?, slot: ItemSlot) {
        val target = target ?: caster.koishify()
        val input = abilityInput(caster.koishify(), target) {
            trigger(trigger)
            manaCost(manaCost)
        }
        instance.record(input, slot)
    }

    fun cast(caster: Player, target: KoishEntity?) {
        val target = target ?: caster.koishify()
        val input = abilityInput(caster.koishify(), target) {
            trigger(trigger)
            manaCost(manaCost)
        }
        instance.cast(input)
    }

    fun saveNbt(): CompoundTag = CompoundTag {
        writeTrigger(trigger)
        writeVariant(variant)
        writeExpression(manaCost)
    }
}

/**
 * [AbilityTriggerVariant] 的序列化器.
 */
internal object TriggerVariantSerializer : ScalarSerializer<AbilityTriggerVariant>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): AbilityTriggerVariant {
        val value = obj.toString()

        try {
            return AbilityTriggerVariant.of(value.toInt())
        } catch (ex: NumberFormatException) {
            throw SerializationException(ex)
        }
    }

    override fun serialize(item: AbilityTriggerVariant?, typeSupported: Predicate<Class<*>>?): Any {
        throw UnsupportedOperationException()
    }

    override fun emptyValue(specificType: Type, options: ConfigurationOptions): AbilityTriggerVariant {
        return AbilityTriggerVariant.any()
    }
}

//<editor-fold desc="Convenient extension functions">
private const val NBT_ABILITY_TRIGGER = "trigger"
private const val NBT_ABILITY_TRIGGER_VARIANT = "variant"
private const val NBT_ABILITY_MANA_COST = "mana_cost"

private fun CompoundTag.readTrigger(): AbilityTrigger? {
    return getStringOrNull(NBT_ABILITY_TRIGGER)?.let { KoishRegistries2.ABILITY_TRIGGER[Identifiers.of(it)] }
}

private fun CompoundTag.readVariant(): AbilityTriggerVariant {
    val variant = this.getIntOrNull(NBT_ABILITY_TRIGGER_VARIANT)
    if (variant == null)
        return AbilityTriggerVariant.any()
    return AbilityTriggerVariant.of(variant)
}

private fun CompoundTag.readExpression(): Expression {
    return getStringOrNull(NBT_ABILITY_MANA_COST)?.let { Expression.of(it) } ?: Expression.of(0)
}

private fun CompoundTag.writeTrigger(trigger: AbilityTrigger?) {
    if (trigger == null)
        return
    putString(NBT_ABILITY_TRIGGER, trigger.id)
}

private fun CompoundTag.writeVariant(variant: AbilityTriggerVariant) {
    if (variant == AbilityTriggerVariant.any())
        return
    putInt(NBT_ABILITY_TRIGGER_VARIANT, variant.id)
}

private fun CompoundTag.writeExpression(expression: Expression) {
    putString(NBT_ABILITY_MANA_COST, expression.asString())
}
//</editor-fold>
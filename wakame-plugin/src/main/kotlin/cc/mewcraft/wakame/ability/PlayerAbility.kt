package cc.mewcraft.wakame.ability

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.BinarySerializable
import cc.mewcraft.wakame.ability.character.Caster
import cc.mewcraft.wakame.ability.character.Target
import cc.mewcraft.wakame.ability.context.abilityInput
import cc.mewcraft.wakame.ability.trigger.SingleTrigger
import cc.mewcraft.wakame.ability.trigger.Trigger
import cc.mewcraft.wakame.ability.trigger.TriggerVariant
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.molang.Evaluable
import cc.mewcraft.wakame.registry.AbilityRegistry
import cc.mewcraft.wakame.util.*
import cc.mewcraft.wakame.util.text.mini
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.function.Predicate


/**
 * 本函数用于直接构建 [PlayerAbility].
 *
 * @param id 技能的唯一标识, 也就是 [PlayerAbility.id]
 * @param trigger 触发此技能的触发器
 * @param variant 可以触发此技能的物品变体
 *
 * @return 构建的 [PlayerAbility]
 */
fun PlayerAbility(
    id: Key, trigger: Trigger, variant: TriggerVariant, manaCost: Evaluable<*>,
): PlayerAbility {
    return SimplePlayerAbility(id, trigger, variant, manaCost)
}

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
    val manaCost = tag.readEvaluable()
    return SimplePlayerAbility(id, trigger, variant, manaCost)
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
    val trigger = node.node("trigger").get<Trigger>() ?: SingleTrigger.NOOP
    val variant = node.node("variant").require<TriggerVariant>()
    val manaCost = node.node("mana_cost").require<Evaluable<*>>()
    return SimplePlayerAbility(id, trigger, variant, manaCost)
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
// TODO 改成 class, 不要 interface - 此处接口没有实际作用
interface PlayerAbility : BinarySerializable<CompoundTag> {
    /**
     * 技能的唯一标识.
     */
    val id: Key

    /**
     * 触发此技能的触发器.
     */
    val trigger: Trigger

    /**
     * 可以触发此技能的物品变体.
     */
    val variant: TriggerVariant

    /**
     * 获取对应的 [Ability] 实例.
     */
    val instance: Ability

    /**
     * 技能的法力消耗.
     */
    val manaCost: Evaluable<*>

    /**
     * 技能的显示名称.
     */
    val displayName: Component

    /**
     * 技能的完整描述.
     */
    val description: List<Component>

    /**
     * 使用 [caster] 记录技能的信息到 ECS 中.
     */
    fun recordBy(caster: Caster, target: Target?, holdBy: Pair<ItemSlot, NekoStack>?)

    /**
     * 将此对象序列化为 NBT, 拥有以下结构:
     *
     * ```NBT
     * string('trigger'): <trigger>
     * int('variant'): <variant>
     * ```
     *
     * 请注意本序列化不包含 [id].
     */
    override fun serializeAsTag(): CompoundTag
}

/**
 * [PlayerAbility] 的标准实现.
 */
internal data class SimplePlayerAbility(
    override val id: Key,
    override val trigger: Trigger,
    override val variant: TriggerVariant,
    override val manaCost: Evaluable<*>,
) : PlayerAbility {
    override val instance: Ability
        get() = AbilityRegistry.INSTANCES[id]
    override val displayName: Component
        get() = instance.displays.name.mini
    override val description: List<Component>
        get() = instance.displays.tooltips.mini

    override fun recordBy(caster: Caster, target: Target?, holdBy: Pair<ItemSlot, NekoStack>?) {
        val input = abilityInput(caster) {
            trigger(trigger)
            manaCost(manaCost)
            holdBy(holdBy)
            target?.let { target(it) }
        }
        instance.recordBy(input)
    }

    override fun serializeAsTag(): CompoundTag = CompoundTag {
        writeTrigger(trigger)
        writeVariant(variant)
        writeEvaluable(manaCost)
    }
}

/**
 * [TriggerVariant] 的序列化器.
 */
internal object TriggerVariantSerializer : ScalarSerializer<TriggerVariant>(typeTokenOf()) {
    override fun deserialize(type: Type, obj: Any): TriggerVariant {
        val value = obj.toString()

        try {
            return TriggerVariant.of(value.toInt())
        } catch (ex: NumberFormatException) {
            throw SerializationException(ex)
        }
    }

    override fun serialize(item: TriggerVariant?, typeSupported: Predicate<Class<*>>?): Any {
        throw UnsupportedOperationException()
    }

    override fun emptyValue(specificType: Type, options: ConfigurationOptions): TriggerVariant {
        return TriggerVariant.any()
    }
}

//<editor-fold desc="Convenient extension functions">
private const val NBT_ABILITY_TRIGGER = "trigger"
private const val NBT_ABILITY_TRIGGER_VARIANT = "variant"
private const val NBT_ABILITY_MANA_COST = "mana_cost"

private fun CompoundTag.readTrigger(): Trigger {
    return getStringOrNull(NBT_ABILITY_TRIGGER)?.let { AbilityRegistry.TRIGGERS[Key(it)] } ?: SingleTrigger.NOOP
}

private fun CompoundTag.readVariant(): TriggerVariant {
    val variant = this.getIntOrNull(NBT_ABILITY_TRIGGER_VARIANT)
    if (variant == null)
        return TriggerVariant.any()
    return TriggerVariant.of(variant)
}

private fun CompoundTag.readEvaluable(): Evaluable<*> {
    return getStringOrNull(NBT_ABILITY_MANA_COST)?.let { Evaluable.parseExpression(it) } ?: Evaluable.parseNumber(0)
}

private fun CompoundTag.writeTrigger(trigger: Trigger) {
    if (trigger == SingleTrigger.NOOP)
        return
    putString(NBT_ABILITY_TRIGGER, trigger.key.asString())
}

private fun CompoundTag.writeVariant(variant: TriggerVariant) {
    if (variant == TriggerVariant.any())
        return
    putInt(NBT_ABILITY_TRIGGER_VARIANT, variant.id)
}

private fun CompoundTag.writeEvaluable(evaluable: Evaluable<*>) {
    putString(NBT_ABILITY_MANA_COST, evaluable.asString())
}
//</editor-fold>
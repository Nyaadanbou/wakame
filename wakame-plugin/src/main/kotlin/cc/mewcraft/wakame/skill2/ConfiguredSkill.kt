package cc.mewcraft.wakame.skill2

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.BinarySerializable
import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill2.trigger.SingleTrigger
import cc.mewcraft.wakame.skill2.trigger.Trigger
import cc.mewcraft.wakame.skill2.trigger.TriggerVariant
import cc.mewcraft.wakame.util.*
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.component.get
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.function.Predicate


/**
 * 本函数用于直接构建 [ConfiguredSkill].
 *
 * @param id 技能的唯一标识, 也就是 [ConfiguredSkill.id]
 * @param trigger 触发此技能的触发器
 * @param variant 可以触发此技能的物品变体
 *
 * @return 构建的 [ConfiguredSkill]
 */
fun ConfiguredSkill(
    id: Key, trigger: Trigger, variant: TriggerVariant,
): ConfiguredSkill {
    return SimpleConfiguredSkill(id, trigger, variant)
}

/**
 * 本函数用于从 NBT 构建 [ConfiguredSkill].
 *
 * 给定的 NBT 的结构必须是以下结构:
 *
 * ```NBT
 * string('trigger'): <trigger>
 * int('variant'): <variant>
 * ```
 *
 * @param id 技能的唯一标识, 也就是 [ConfiguredSkill.id]
 * @param tag 包含该技能数据的 NBT
 *
 * @return 从 NBT 构建的 [ConfiguredSkill]
 */
fun ConfiguredSkill(
    id: Key, tag: CompoundTag,
): ConfiguredSkill {
    val trigger = tag.readTrigger()
    val variant = tag.readVariant()
    return SimpleConfiguredSkill(id, trigger, variant)
}

/**
 * 本函数用于从配置文件构建 [ConfiguredSkill].
 *
 * 给定的 [ConfigurationNode] 必须是以下结构:
 *
 * ```yaml
 * <node>:
 *  trigger: <trigger>
 *  variant: <variant>
 * ```
 *
 * @param id 技能的唯一标识, 也就是 [ConfiguredSkill.id]
 * @param node 包含技能数据的配置节点
 *
 * @return 从配置文件构建的 [ConfiguredSkill]
 */
fun ConfiguredSkill(
    id: Key, node: ConfigurationNode,
): ConfiguredSkill {
    val trigger = node.node("trigger").get<Trigger>() ?: SingleTrigger.NOOP
    val variant = node.node("variant").krequire<TriggerVariant>()
    return SimpleConfiguredSkill(id, trigger, variant)
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
interface ConfiguredSkill : BinarySerializable<CompoundTag> {
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
     * 获取对应的 [Skill] 实例.
     */
    val instance: Skill

    /**
     * 技能的显示名称.
     */
    val displayName: Component

    /**
     * 技能的完整描述.
     */
    val description: List<Component>

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
 * [ConfiguredSkill] 的标准实现.
 */
internal data class SimpleConfiguredSkill(
    override val id: Key,
    override val trigger: Trigger,
    override val variant: TriggerVariant,
) : ConfiguredSkill {
    override val instance: Skill
        get() = SkillRegistry.INSTANCES[id]
    override val displayName: Component
        get() = instance.displays.name.let(MM::deserialize)
    override val description: List<Component>
        get() = instance.displays.tooltips.map(MM::deserialize)

    override fun serializeAsTag(): CompoundTag = CompoundTag {
        writeTrigger(trigger)
        writeVariant(variant)
    }

    companion object Shared {
        private val MM = Injector.get<MiniMessage>()
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
private const val NBT_SKILL_TRIGGER = "trigger"
private const val NBT_SKILL_TRIGGER_VARIANT = "variant"

private fun CompoundTag.readTrigger(): Trigger {
    return getStringOrNull(NBT_SKILL_TRIGGER)?.let { SkillRegistry.TRIGGERS[Key(it)] } ?: SingleTrigger.NOOP
}

private fun CompoundTag.readVariant(): TriggerVariant {
    val variant = this.getIntOrNull(NBT_SKILL_TRIGGER_VARIANT)
    if (variant == null)
        return TriggerVariant.any()
    return TriggerVariant.of(variant)
}

private fun CompoundTag.writeTrigger(trigger: Trigger) {
    if (trigger == SingleTrigger.NOOP)
        return
    putString(NBT_SKILL_TRIGGER, trigger.key.asString())
}

private fun CompoundTag.writeVariant(variant: TriggerVariant) {
    if (variant == TriggerVariant.any())
        return
    putInt(NBT_SKILL_TRIGGER_VARIANT, variant.id)
}
//</editor-fold>
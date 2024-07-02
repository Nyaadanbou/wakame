package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.item.binary.cell.core.skill.BinarySkillCore
import cc.mewcraft.wakame.skill.trigger.Trigger
import cc.mewcraft.wakame.skill.trigger.TriggerVariant
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.function.Predicate

/**
 * 构建一个 [ConfiguredSkill].
 */
fun ConfiguredSkill(core: BinarySkillCore): ConfiguredSkill {
    return ConfiguredSkill(core.key, core.trigger, core.variant)
}

/**
 * 代表一个物品词条栏中的技能.
 */
data class ConfiguredSkill(
    /**
     * 技能的唯一标识(带文件夹的那种).
     */
    val key: Key,
    /**
     * 触发此技能的触发器.
     */
    val trigger: Trigger,
    /**
     * 可以触发此技能的物品变体.
     *
     * 这是做什么用的?
     * 如果物品变体不匹配, 即使玩家按对了触发器 [trigger], 技能最终也不会释放.
     */
    val variant: TriggerVariant,
) {
}

internal object ConfiguredSkillVariantSerializer : ScalarSerializer<TriggerVariant>(typeTokenOf()) {
    override fun deserialize(type: Type?, obj: Any?): TriggerVariant {
        obj ?: return TriggerVariant.any()

        val value = obj.toString()
        return try {
            TriggerVariant.of(value.toInt())
        } catch (ex: NumberFormatException) {
            throw SerializationException(ex)
        }
    }

    override fun serialize(item: TriggerVariant?, typeSupported: Predicate<Class<*>>?): Any {
        throw UnsupportedOperationException()
    }
}

internal object ConfiguredSkillSerializer : SchemaSerializer<ConfiguredSkill> {
    override fun deserialize(type: Type, node: ConfigurationNode): ConfiguredSkill {
        val key = node.node("key").krequire<Key>()
        val trigger = node.node("trigger").krequire<Trigger>()
        val variantNode = node.node("variant")
        val variant = if (variantNode.isNull) TriggerVariant.any() else variantNode.krequire<TriggerVariant>()
        return ConfiguredSkill(key, trigger, variant)
    }
}
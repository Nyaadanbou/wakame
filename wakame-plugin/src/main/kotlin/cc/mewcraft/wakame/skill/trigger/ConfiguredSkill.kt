package cc.mewcraft.wakame.skill.trigger

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.item.binary.cell.core.skill.BinarySkillCore
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.function.Predicate

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
    val variant: Variant,
) {
    /**
     * 代表一个可以触发此技能的物品变体.
     */
    interface Variant {
        val variant: Int

        companion object {
            /**
             * 返回一个代表任意 [Variant] 的实例.
             */
            fun any(): Variant = VariantImpl.AnyVariant

            /**
             * 从整数创建一个 [Variant].
             */
            fun of(variant: Int): Variant = VariantImpl(variant)
        }
    }

    private data class VariantImpl(
        override val variant: Int,
    ) : Variant {
        data object AnyVariant : Variant {
            override val variant: Int = -1
        }
    }

    internal object VariantSerializer : ScalarSerializer<Variant>(typeTokenOf()) {
        override fun deserialize(type: Type?, obj: Any?): Variant {
            obj ?: return Variant.any()

            val value = obj.toString()
            return try {
                Variant.of(value.toInt())
            } catch (ex: NumberFormatException) {
                throw SerializationException(ex)
            }
        }

        override fun serialize(item: Variant?, typeSupported: Predicate<Class<*>>?): Any {
            throw UnsupportedOperationException()
        }
    }
}

fun ConfiguredSkill(core: BinarySkillCore): ConfiguredSkill {
    return ConfiguredSkill(core.key, core.trigger, core.effectiveVariant)
}

internal object ConfiguredSkillSerializer : SchemaSerializer<ConfiguredSkill> {
    override fun deserialize(type: Type, node: ConfigurationNode): ConfiguredSkill {
        val key = node.node("key").krequire<Key>()
        val trigger = node.node("trigger").krequire<Trigger>()
        val variantNode = node.node("variant")
        val variant = if (variantNode.isNull) ConfiguredSkill.Variant.any() else variantNode.krequire<ConfiguredSkill.Variant>()
        return ConfiguredSkill(key, trigger, variant)
    }
}
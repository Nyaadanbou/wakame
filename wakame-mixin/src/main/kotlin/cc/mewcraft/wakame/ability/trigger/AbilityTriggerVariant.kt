package cc.mewcraft.wakame.ability.trigger

import cc.mewcraft.wakame.util.typeTokenOf
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.function.Predicate

/**
 * 代表一个可以触发此技能的物品变体.
 *
 * ## 用途
 * 触发技能的逻辑在检测一个玩家动作是否能触发技能时,
 * 还会额外检测技能的变体是否与物品的变体相匹配.
 * 如果物品变体不匹配, 即使玩家按对了触发器 ([AbilityTrigger]), 技能最终也不会释放.
 */
interface AbilityTriggerVariant {
    /**
     * 变体的唯一标识, 会直接与物品上的变体做比较.
     */
    val id: Int

    companion object {

        @JvmField
        val SERIALIZER: ScalarSerializer<AbilityTriggerVariant> = Serializer

        /**
         * 返回一个代表任意 [AbilityTriggerVariant] 的实例.
         */
        fun any(): AbilityTriggerVariant = Any

        /**
         * 从整数创建一个 [AbilityTriggerVariant].
         */
        fun of(variant: Int): AbilityTriggerVariant = Impl(variant)
    }

    private data class Impl(override val id: Int) : AbilityTriggerVariant {
        init {
            require(id != -1) {
                "Cannot create a variant with id '-1'. Use AbilityTriggerVariant.any() if you want to create an variant that represents any variant"
            }
        }
    }

    private data object Any : AbilityTriggerVariant {
        override val id: Int = -1 // magic value
    }

    private object Serializer : ScalarSerializer<AbilityTriggerVariant>(typeTokenOf()) {
        override fun deserialize(type: Type, obj: kotlin.Any): AbilityTriggerVariant {
            val value = obj.toString()

            try {
                return of(value.toInt())
            } catch (ex: NumberFormatException) {
                throw SerializationException(ex)
            }
        }

        override fun serialize(item: AbilityTriggerVariant, typeSupported: Predicate<Class<*>>): kotlin.Any {
            return item.id.toString()
        }

        override fun emptyValue(specificType: Type, options: ConfigurationOptions): AbilityTriggerVariant {
            return any()
        }
    }
}

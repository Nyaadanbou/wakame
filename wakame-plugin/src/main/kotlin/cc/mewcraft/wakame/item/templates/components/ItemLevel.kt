package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.*
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import cc.mewcraft.wakame.item.components.ItemLevel as ItemLevelData


/**
 * 物品的等级(模板).
 *
 * 等级的生成目前有两种模式: 固定等级, 动态等级.
 *
 * # 固定等级
 * 直接在配置文件中指定好一个常数, 然后每次都按照该常数生成等级.
 *
 * # 动态等级
 * 由生成的上下文决定要生成的等级.
 */
data class ItemLevel(
    private val level: Any,
) : ItemTemplate<ItemLevelData> {

    companion object: ItemTemplateBridge<ItemLevel> {
        override fun codec(id: String): ItemTemplateType<ItemLevel> {
            return Codec(id)
        }
    }

    /**
     * 检查等级是否为固定的.
     */
    val isConstant: Boolean = level is Number

    /**
     * 检查等级是否基于上下文.
     */
    val isContextual: Boolean = level == Option.CONTEXT

    override val componentType: ItemComponentType<ItemLevelData> = ItemComponentTypes.LEVEL

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<ItemLevelData> {
        val raw: Int = when (level) {
            is Number -> {
                level.toStableInt()
            }

            is Option -> {
                when (level) {
                    Option.CONTEXT -> context.trigger.level
                }
            }

            else -> {
                throw IllegalStateException("Something wrong with ${this::class.simpleName}")
            }
        }

        return raw
            .coerceAtLeast(0) // by design, level never goes down below 0
            .also { context.level = it } // populate the context with generated level
            .let { ItemGenerationResult.of(ItemLevelData(level = it)) }
    }

    enum class Option {
        CONTEXT
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemLevel> {
        override val type: TypeToken<ItemLevel> = typeTokenOf()

        /**
         * ## Node structure 1
         * ```yaml
         * <node>: <int>
         * ```
         *
         * ## Node structure 2
         * ```yaml
         * <node>: <enum>
         * ```
         */
        override fun decode(node: ConfigurationNode): ItemLevel {
            return when (val scalar = node.rawScalar()) {
                is Number -> ItemLevel(scalar)
                is String -> ItemLevel(EnumLookup.lookup<Option>(scalar).getOrThrow())
                else -> throw SerializationException(node, type.type, "Invalid value type")
            }
        }
    }
}
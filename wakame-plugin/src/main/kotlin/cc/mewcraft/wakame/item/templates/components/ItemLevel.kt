package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.*
import com.google.common.collect.Range
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.SerializationException
import kotlin.random.nextInt
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
 *
 * @param base 等级的基础值
 * @param floatChance 等级浮动的概率
 * @param floatAmount 等级浮动的范围
 * @param max 等级最终的最大值
 */
data class ItemLevel(
    private val base: Any,
    private val floatChance: Double,
    private val floatAmount: IntRange,
    private val max: Int,
) : ItemTemplate<ItemLevelData> {

    companion object : ItemTemplateBridge<ItemLevel> {
        override fun codec(id: String): ItemTemplateType<ItemLevel> {
            return Codec(id)
        }
    }

    /**
     * 检查等级是否为固定的.
     */
    val isConstant: Boolean = base is Number

    /**
     * 检查等级是否基于上下文.
     */
    val isContextual: Boolean = base == Option.CONTEXT

    override val componentType: ItemComponentType<ItemLevelData> = ItemComponentTypes.LEVEL

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<ItemLevelData> {
        val raw: Int = when (base) {
            is Number -> {
                base.toStableInt() + (if (context.random.nextDouble() < floatChance) context.random.nextInt(floatAmount) else 0)
            }

            is Option -> {
                when (base) {
                    Option.CONTEXT -> context.trigger.level + (if (context.random.nextDouble() < floatChance) context.random.nextInt(floatAmount) else 0)
                }
            }

            else -> {
                throw IllegalStateException("Something wrong with ${this::class.simpleName}")
            }
        }

        return raw
            .coerceIn(ItemLevelData.minimumLevel, max)
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
         * <node>:
         *   base: <int>
         *   float_chance: <double>
         *   float_amount: <string>
         *   max: <int>
         * ```
         *
         * ## Node structure 2
         * ```yaml
         * <node>:
         *   base: <enum>
         *   float_chance: <double>
         *   float_amount: <string>
         *   max: <int>
         * ```
         */
        override fun decode(node: ConfigurationNode): ItemLevel {
            val base = when (val scalar = node.node("base").rawScalar()) {
                is Number -> scalar
                is String -> EnumLookup.lookup<Option>(scalar).getOrThrow()
                else -> throw SerializationException(node, type.type, "Invalid value type")
            }
            val floatChance = node.node("float_chance").get<Double>(.0).takeIf { it in 0.0..1.0 } ?: throw SerializationException(node, type.type, "Invalid float_chance range")
            val floatAmount = node.node("float_amount").get<Range<Int>>(Range.closed(0, 0)).toKotlinRange() ?: throw SerializationException(node, type.type, "Invalid float_amount range")
            val max = node.node("max").get<Int>() ?: Int.MAX_VALUE
            return ItemLevel(base, floatChance, floatAmount, max)
        }
    }
}
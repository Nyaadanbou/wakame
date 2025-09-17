package cc.mewcraft.wakame.item2.config.datagen.impl

import cc.mewcraft.wakame.item2.config.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaResult
import cc.mewcraft.wakame.item2.context.ItemGenerationContext
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.item2.data.impl.ItemLevel
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.EnumLookup
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.toKotlinRange
import com.google.common.collect.Range
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import kotlin.random.nextInt

/**
 * 物品的等级(模板).
 *
 * 等级的生成目前有两种模式: 固定等级, 动态等级.
 *
 * ### 固定等级
 * 直接在配置文件中指定好一个常数, 然后每次都按照该常数生成等级.
 *
 * ### 动态等级
 * 由生成的上下文决定要生成的等级.
 *
 * @param base 等级的基础值
 * @param floatChance 等级浮动的概率
 * @param floatAmount 等级浮动的范围
 * @param max 等级最终的最大值
 */
data class MetaItemLevel(
    private val base: Any,
    private val floatChance: Double,
    private val floatAmount: IntRange,
    private val max: Int,
) : ItemMetaEntry<ItemLevel> {

    companion object {

        // 实现注意事项: 将内部的 TypeSerializer 暴露为一个 val
        @JvmField
        val SERIALIZER: TypeSerializer2<MetaItemLevel> = Serializer

    }

    override fun make(context: ItemGenerationContext): ItemMetaResult<ItemLevel> {
        val raw: Int = when (base) {
            is Number -> {
                base.toInt() + (if (context.random.nextDouble() < floatChance) context.random.nextInt(floatAmount) else 0)
            }

            is Option -> {
                when (base) {
                    Option.CONTEXT -> context.level + (if (context.random.nextDouble() < floatChance) context.random.nextInt(floatAmount) else 0)
                }
            }

            else -> {
                throw IllegalStateException("Unrecognized base type: ${base::class}")
            }
        }

        return raw
            .coerceIn(ItemLevel.minimumLevel, max)
            .also { lvl -> context.level = lvl } // populate the context with generated level
            .let { lvl -> ItemMetaResult.of(ItemLevel(lvl)) }
    }

    override fun write(value: ItemLevel, itemstack: MojangStack) {
        itemstack.ensureSetData(ItemDataTypes.LEVEL, value)
    }

    private enum class Option {
        CONTEXT
    }

    /**
     * ## Node structure
     * ```yaml
     * <node>:
     *   base: <int> OR <enum>
     *   float_chance: <double>
     *   float_amount: <string>
     *   max: <int>
     * ```
     */
    private object Serializer : TypeSerializer2<MetaItemLevel> {
        override fun deserialize(type: Type, node: ConfigurationNode): MetaItemLevel {
            val base = when (val scalar = node.node("base").rawScalar()) {
                is Number -> scalar
                is String -> EnumLookup.lookup<Option>(scalar).getOrThrow()
                else -> throw SerializationException(node, type, "invalid `base`")
            }
            val floatChance = node.node("float_chance").get<Double>(.0).takeIf { it in 0.0..1.0 } ?: throw SerializationException(node, type, "invalid `float_chance`")
            val floatAmount = node.node("float_amount").get<Range<Int>>(Range.closed(0, 0)).toKotlinRange() ?: throw SerializationException(node, type, "invalid `float_amount`")
            val max = node.node("max").get<Int>() ?: Int.MAX_VALUE
            return MetaItemLevel(base, floatChance, floatAmount, max)
        }
    }

}
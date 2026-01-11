package cc.mewcraft.wakame.item.datagen.impl

import cc.mewcraft.wakame.item.data.ItemDataTypes
import cc.mewcraft.wakame.item.data.impl.ItemLevel
import cc.mewcraft.wakame.item.datagen.ItemGenerationContext
import cc.mewcraft.wakame.item.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item.datagen.ItemMetaResult
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.serialization.configurate.serializer.DispatchingSerializer
import cc.mewcraft.wakame.util.MojangStack
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting
import kotlin.random.nextInt

/**
 * 物品的等级(模板).
 *
 * 等级的生成目前有三种模式: 常量等级、浮动等级、上下文等级.
 *
 * ### 常量等级 (Constant)
 * 每次生成都是相同的等级，不会浮动.
 *
 * ### 浮动等级 (Floating)
 * 使用配置文件中指定的基础值，可以根据概率进行浮动.
 *
 * ### 上下文等级 (Contextual)
 * 由生成的上下文决定基础等级，可以根据概率进行浮动.
 */
sealed interface MetaItemLevel : ItemMetaEntry<ItemLevel> {

    companion object {

        @JvmField
        val SERIALIZER: TypeSerializer2<MetaItemLevel> = DispatchingSerializer.createPartial<String, MetaItemLevel>(
            mapOf(
                "constant" to Constant::class,
                "floating" to Floating::class,
                "contextual" to Contextual::class,
            )
        )
    }

    // Hint:
    // 尽管 MetaItemLevel 有多个实现, 但其 write 函数体都是一样的.
    // 因此可以直接在接口定义 write 函数的实现,
    // 其余的实现类只需要重写 make 函数即可.
    override fun write(value: ItemLevel, itemstack: MojangStack) {
        itemstack.ensureSetData(ItemDataTypes.LEVEL, value)
    }

    /**
     * 常量等级模式.
     *
     * 每次生成都是相同的等级，不会浮动.
     *
     * @param value 固定的等级值
     */
    @ConfigSerializable
    data class Constant(
        @Setting("value")
        val value: Int,
    ) : MetaItemLevel {

        override fun randomized(): Boolean {
            return false
        }

        override fun make(context: ItemGenerationContext): ItemMetaResult<ItemLevel> {
            val level = value.coerceIn(ItemLevel.MINIMUM_LEVEL, Int.MAX_VALUE)
            context.level = level // populate the context with generated level
            return ItemMetaResult.of(ItemLevel(level))
        }
    }

    /**
     * 浮动等级模式.
     *
     * 使用配置文件中指定的基础值，可以根据概率进行浮动.
     *
     * @param base 等级的基础值
     * @param floatChance 等级浮动的概率
     * @param floatAmount 等级浮动的范围
     * @param max 等级最终的最大值
     */
    @ConfigSerializable
    data class Floating(
        @Setting("base")
        val base: Int,
        @Setting("float_chance")
        val floatChance: Double = 0.0,
        @Setting("float_amount")
        val floatAmount: IntRange = 0..0,
        @Setting("max")
        val max: Int = Int.MAX_VALUE,
    ) : MetaItemLevel {

        override fun randomized(): Boolean {
            return floatChance > 0.0 && floatAmount != 0..0
        }

        override fun make(context: ItemGenerationContext): ItemMetaResult<ItemLevel> {
            val raw = base + (if (context.random.nextDouble() < floatChance) context.random.nextInt(floatAmount) else 0)
            val level = raw.coerceIn(ItemLevel.MINIMUM_LEVEL, max)
            context.level = level // populate the context with generated level
            return ItemMetaResult.of(ItemLevel(level))
        }
    }

    /**
     * 上下文等级模式.
     *
     * 由生成的上下文决定基础等级，可以根据概率进行浮动.
     *
     * @param floatChance 等级浮动的概率
     * @param floatAmount 等级浮动的范围
     * @param max 等级最终的最大值
     */
    @ConfigSerializable
    data class Contextual(
        @Setting("float_chance")
        val floatChance: Double = 0.0,
        @Setting("float_amount")
        val floatAmount: IntRange = 0..0,
        @Setting("max")
        val max: Int = Int.MAX_VALUE,
    ) : MetaItemLevel {

        override fun randomized(): Boolean {
            return true
        }

        override fun make(context: ItemGenerationContext): ItemMetaResult<ItemLevel> {
            val raw = context.level + (if (context.random.nextDouble() < floatChance) context.random.nextInt(floatAmount) else 0)
            val level = raw.coerceIn(ItemLevel.MINIMUM_LEVEL, max)
            context.level = level // populate the context with generated level
            return ItemMetaResult.of(ItemLevel(level))
        }
    }
}
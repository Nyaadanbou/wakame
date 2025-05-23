package cc.mewcraft.wakame.reforge.common

import cc.mewcraft.wakame.util.adventure.toSimpleString
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.commons.collections.enumMap
import java.util.*
import java.util.Collections.emptyMap
import java.util.stream.Stream

/**
 * 一个价格计算器, 设计上类似“属性实例”.
 */
class PriceInstance(
    minimumBaseValue: Double,
    maximumBaseValue: Double,
    priceModifiers: Map<String, PriceModifier>,
) : Examinable {

    constructor(
        baseValue: Double,
        priceModifiers: Map<String, PriceModifier>,
    ) : this(
        // min = max
        baseValue,
        baseValue,
        //
        priceModifiers
    )

    private val minimumBaseValue = minimumBaseValue.coerceAtLeast(.0)
    private val maximumBaseValue = maximumBaseValue.coerceAtLeast(.0)
    private val priceModifiersById: Object2ObjectOpenHashMap<String, PriceModifier> = Object2ObjectOpenHashMap()
    private val priceModifiersByOp: EnumMap<PriceModifier.Operation, Object2ObjectOpenHashMap<String, PriceModifier>> = enumMap()

    init {
        priceModifiers.forEach { (name, modifier) ->
            priceModifiersById[name] = modifier
            priceModifiersByOp.computeIfAbsent(modifier.operation) { Object2ObjectOpenHashMap() }[name] = modifier
        }
    }

    /**
     * 方便函数.
     *
     * 当客户端代码已经知晓 [minimumBaseValue] 与 [maximumBaseValue]
     * 是相等的时候, 可以直接使用这个函数. 该函数相当于 [getMinimumValue].
     */
    fun getValue(item: ItemStack): Double {
        return getValue(item, minimumBaseValue)
    }

    fun getMinimumValue(item: ItemStack): Double {
        return getValue(item, minimumBaseValue)
    }

    fun getMaximumValue(item: ItemStack): Double {
        return getValue(item, maximumBaseValue)
    }

    private fun getValue(item: ItemStack, baseValue: Double): Double {
        var x: Double = baseValue
        getModifiersOrEmpty(PriceModifier.Operation.ADD_VALUE).forEach { x += it.value.evaluate(item) }
        var y: Double = x
        getModifiersOrEmpty(PriceModifier.Operation.ADD_MULTIPLIED_BASE).forEach { y += x * it.value.evaluate(item) }
        getModifiersOrEmpty(PriceModifier.Operation.ADD_MULTIPLIED_TOTAL).forEach { y *= 1.0 + it.value.evaluate(item) }

        // 这里的价格为单价, 所以乘以物品堆叠的数量
        y *= item.amount
        // 无论如何价格都不应该为负数
        y = y.coerceAtLeast(.0)

        return y
    }

    private fun getModifiersOrEmpty(operation: PriceModifier.Operation): Map<String, PriceModifier> {
        return priceModifiersByOp.getOrDefault(operation, emptyMap())
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("minimumBaseValue", minimumBaseValue),
            ExaminableProperty.of("maximumBaseValue", maximumBaseValue),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}
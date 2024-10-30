package cc.mewcraft.wakame.reforge.common

import cc.mewcraft.wakame.util.toSimpleString
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.commons.collections.enumMap
import java.util.EnumMap
import java.util.stream.Stream
import kotlin.random.Random

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
    ) : this(baseValue, baseValue, priceModifiers)

    companion object Shared {
        private val ZERO_RANDOM = Random(0)
    }

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

    fun getMinimumValue(item: ItemStack, random: Random = ZERO_RANDOM): Double {
        return getValue(item, minimumBaseValue)
    }

    fun getMaximumValue(item: ItemStack, random: Random): Double {
        return getValue(item, maximumBaseValue)
    }

    // 当 minimumBaseValue 和 maximumBaseValue 相同时, 可以直接用这个函数
    fun getValue(item: ItemStack, random: Random): Double {
        return getValue(item, minimumBaseValue + random.nextDouble() * (maximumBaseValue - minimumBaseValue))
    }

    private fun getValue(item: ItemStack, baseValue: Double): Double {
        var x: Double = baseValue
        getModifiersOrEmpty(PriceModifier.Operation.ADD_VALUE).forEach { x += it.value.evaluate(item) }
        var y: Double = x
        getModifiersOrEmpty(PriceModifier.Operation.ADD_MULTIPLIED_BASE).forEach { y += x * it.value.evaluate(item) }
        getModifiersOrEmpty(PriceModifier.Operation.ADD_MULTIPLIED_TOTAL).forEach { y *= 1.0 + it.value.evaluate(item) }
        return y
    }

    private fun getModifiersOrEmpty(operation: PriceModifier.Operation): Map<String, PriceModifier> {
        return priceModifiersByOp.getOrDefault(operation, emptyMap())
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("baseValue", minimumBaseValue),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}
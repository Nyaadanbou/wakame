package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.item.SlotContent

/**
 * An attribute in a slot.
 */
abstract class Attribute : SlotContent {
    abstract val value: Int

    open fun compute(mods: List<AttributeModifier>): Int {
        var x: Double = value.toDouble()
        mods.filter { it.operation == AttributeModifier.Operation.ADDITION }.forEach { x += it.amount.toDouble() }
        var y: Double = x
        mods.filter { it.operation == AttributeModifier.Operation.MULTIPLY_BASE }.forEach { y += x * it.amount.toDouble() }
        mods.filter { it.operation == AttributeModifier.Operation.MULTIPLY_TOTAL }.forEach { y *= 1.0 + it.amount.toDouble() }
        return y.toInt()
    }
}
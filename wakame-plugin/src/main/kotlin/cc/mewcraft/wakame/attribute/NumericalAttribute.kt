package cc.mewcraft.wakame.attribute

import net.kyori.adventure.key.Key

/**
 * Represents an attribute that can be expressed as a numerical value.
 *
 * Examples of numerical attributes:
 * - attack damage
 * - attack speed
 * - maximum health
 * - defense
 */
open class NumericalAttribute(
    override val attributeKey: Key,
    val defaultValue: Double,
) : Attribute {
    override fun equals(other: Any?): Boolean {
        return other is NumericalAttribute && other.attributeKey == this.attributeKey
    }

    override fun hashCode(): Int {
        return attributeKey.hashCode()
    }
}
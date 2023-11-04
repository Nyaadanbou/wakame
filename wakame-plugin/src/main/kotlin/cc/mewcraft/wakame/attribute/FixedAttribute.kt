package cc.mewcraft.wakame.attribute

/**
 * An attribute that can be expressed as a fixed numerical value.
 * The same fixed value are returned for each invocation of [value].
 *
 * Examples of fixed attributes:
 * - defense
 * - maximum health
 * - maximum mana
 */
abstract class FixedAttribute(
    var base: Int,
) : Attribute() {
    override val value: Int
        get() = base
}
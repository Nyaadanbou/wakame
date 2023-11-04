package cc.mewcraft.wakame.attribute

import kotlin.random.Random

/**
 * An attribute that can be expressed as a numerical value that can
 * vary between the [min] and [max] for each invocation of [value].
 *
 * Examples of random attributes:
 * - neutral attack damage
 * - element attack damage
 */
abstract class RandomAttribute(
    var min: Int,
    var max: Int,
) : Attribute() {
    override val value: Int
        get() = if (min == max) min else Random.nextInt(min, max)
}
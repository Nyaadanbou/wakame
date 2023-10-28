package cc.mewcraft.wakame.primitive

import kotlin.random.Random

class FloatingDouble(
    val min: Double,
    val max: Double,
) : Value<Double> {
    override val value: Double
        get() = Random.nextDouble(min, max)
}
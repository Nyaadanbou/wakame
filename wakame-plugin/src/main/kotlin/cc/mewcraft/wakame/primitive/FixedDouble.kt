package cc.mewcraft.wakame.primitive

class FixedDouble(
    val fixed: Double,
) : Value<Double> {
    override val value: Double
        get() = fixed
}
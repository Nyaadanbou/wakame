package cc.mewcraft.wakame.util

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type
import java.util.concurrent.ThreadLocalRandom

/**
 * Represents a numeric value which
 *
 * **EITHER**:
 * - is a fixed value
 *
 * **OR**
 * - follows the normal distribution, and/or
 * - scales with a given scaling factor
 */
data class RandomizedValue(
    /**
     * The base value.
     */
    val base: Double,

    /**
     * The scale value, which scales the [base] value with a given factor.
     *
     * This value can be omitted by setting it to **zero**, so that the final
     * result will be as if there is no scale involved at all.
     */
    val scale: Double = .0,

    /**
     * The standard deviation of the normal distribution.
     *
     * This value can be omitted by setting it to **zero**, so that the final
     * result will be as if there is no normal distribution involved at all.
     */
    val sigma: Double = .0,

    /**
     * The minimum spread of the normal distribution.
     *
     * @see upperBound
     */
    val lowerBound: Double? = null,

    /**
     * The maximum spread of the normal distribution.
     *
     * For normal distribution, there always is that INSANELY SMALL chance of
     * getting an INSANELY LARGE number. For example: At base atk dmg 10, and
     * standard deviation 1:
     * - 68% of rolls will fall between 9 and 11;
     * - 95% of rolls will fall between 8 and 12;
     * - 99.7% of rolls will fall between 7 and 13;
     * - 1E-41% of rolls that will give you an epic 300 dmg sword
     *
     * Whatever, this constrains the maximum of output.
     *
     * @see lowerBound
     */
    val upperBound: Double? = null,
) {

    /**
     * Whether the numeric value has scale enabled or not.
     */
    val isScaled: Boolean
        get() = scale != .0

    /**
     * Whether the numeric value has random enabled or not.
     */
    val isRandom: Boolean
        get() = sigma > .0

    /**
     * Whether the random value is bounded by the [lowerBound].
     */
    val isLowerBounded: Boolean
        get() = lowerBound != null

    /**
     * Whether the random value is bounded by the [upperBound].
     */
    val isUpperBounded: Boolean
        get() = upperBound != null

    private constructor(
        base: Number,
        scale: Number = .0,
        sigma: Number = .0,
        lowerBound: Number? = null,
        upperBound: Number? = null,
    ) : this(
        base = base.toDouble(), scale = scale.toDouble(), sigma = sigma.toDouble(), lowerBound = lowerBound?.toDouble(), upperBound = upperBound?.toDouble()
    )

    init {
        // check(scale >= 0) { "scale must not be negative" } // 应该允许为负, 不然没法产生等级越高数值越小的结果
        check(sigma >= 0) { "sigma must not be negative" }
        if (lowerBound != null) {
            check(lowerBound <= 0) { "lowerBound must be negative or zero" }
        }
        if (upperBound != null) {
            check(upperBound >= 0) { "upperBound must be positive or zero" }
        }
    }

    data class Result(
        val value: Double,
        val score: Double,
    )

    companion object Factory {
        /**
         * Creates an instance from a string.
         *
         * @param value a string in the format
         * `base{,scale{,sigma{,lowerBound{,upperBound}}}}`,
         * where the values inside curly brackets can be omitted
         * @return an instance
         */
        fun create(value: String): RandomizedValue {
            val split = value.split(",").dropLastWhile { it.isEmpty() }.toTypedArray()
            val base = split[0].toDouble()
            val scale = if (split.size > 1) split[1].toDouble() else 0.0
            val sigma = if (split.size > 2) split[2].toDouble() else 0.0
            val lowerBound = if (split.size > 3) split[3].toDoubleOrNull() else null
            val upperBound = if (split.size > 4) split[4].toDoubleOrNull() else null
            return RandomizedValue(
                base = base, scale = scale, sigma = sigma, lowerBound = lowerBound, upperBound = upperBound
            )
        }

        /**
         * Creates an instance from a single number.
         *
         * @param base a base value
         * @return an instance
         */
        fun create(base: Number): RandomizedValue {
            return RandomizedValue(base)
        }

        /**
         * Creates an instance from a [configuration node][ConfigurationNode].
         *
         * @param node a configuration node, see the implementation of
         *     [ConfigSerializer] for the supported structures
         * @return an instance
         */
        fun create(node: ConfigurationNode): RandomizedValue {
            return RandomizedValueSerializer.deserialize(javaTypeOf<RandomizedValue>(), node)
        }
    }

    /**
     * Calculates the value.
     *
     * @param scalingFactor scale factor
     * @param randomVariable value of `Random.nextGaussian()`, also known as
     *     Z-score in the context of normal distribution
     * @return the calculated value
     */
    fun calculate(scalingFactor: Double = .0, randomVariable: Double = ThreadLocalRandom.current().nextGaussian()): Result {
        // The mean (mu), the center of the distribution
        val scaledBase = base + (scale * scalingFactor)

        /*
           According to Z-score formula:
                z = (x - mu) / sigma
           where
                z is Z-score,
                x is the value before standardization,
                mu is mean,
                sigma is standard deviation,
           and
                x is also the value we want to calculate.

           Transform the formula, we get:
                x = z * sigma + mu
           Note that we already know these values:
                z, sigma, mu
        */

        // Calculate "z * sigma" (spread), applying thresholds as specified
        val min = lowerBound ?: (sigma * -3)
        val max = upperBound ?: (sigma * +3)
        val spread = (randomVariable * sigma).coerceIn(
            //        ^z-score         ^sigma
            min,
            //        ^min spread
            max
            //        ^max spread
        )

        // spread / sigma = random var
        return Result(
            // Since the mean (mu) might be scaled,
            // we can't simply do `x = z * sigma + mu`.
            // Instead, we calculate the relative value:
            value = scaledBase * (1 + spread),
            // The score is the z-score itself,
            // but respects the min/max spread.
            score = if (sigma != .0) {
                randomVariable.coerceIn(
                    min / sigma,
                    max / sigma
                )
            } else {
                .0 // 当 sigma 为 0 时, 结果相当于固定值, 因此不存在数值质量一说 - 始终返回 0 即可
            }
        )
    }

    /**
     * Calculates the value.
     *
     * This function is the same as [calculate] but different in that it
     * accepts abstract [Numbers][Number] instead of only [Double]. The
     * given [Numbers][Number] values are converted to [Double] values using
     * [Number.toDouble].
     */
    fun calculate(scalingFactor: Number = .0, randomVariable: Number = ThreadLocalRandom.current().nextGaussian()): Result {
        return calculate(scalingFactor = scalingFactor.toDouble(), randomVariable = randomVariable.toDouble())
    }

    /**
     * Calculates the value.
     *
     * This is equivalent to calling [calculate] with [scaling factor] **zero**
     * and a random variable from the standard normal distribution.
     */
    fun calculate(): Result {
        return calculate(.0)
    }
}

internal object RandomizedValueSerializer : TypeSerializer<RandomizedValue> {
    override fun deserialize(type: Type, node: ConfigurationNode): RandomizedValue {
        val scalar = node.rawScalar()
        if (scalar != null) {
            // if it's just a simple plain value like "value: 32"
            return RandomizedValue(node.double, .0, .0, lowerBound = null, upperBound = null)
        }

        val base = node.node("base").require<Double>()
        val scale = node.node("scale").takeIf { !it.virtual() }?.apply { require(Double::class.java) }?.double
        val sigma = node.node("spread").takeIf { !it.virtual() }?.apply { require(Double::class.java) }?.double
        val lowerBound = node.node("min").takeIf { !it.virtual() }?.apply { require(Double::class.java) }?.double
        val upperBound = node.node("max").takeIf { !it.virtual() }?.apply { require(Double::class.java) }?.double

        return RandomizedValue(
            base = base, scale = scale ?: .0, sigma = sigma ?: .0, lowerBound = lowerBound, upperBound = upperBound
        )
    }

    override fun serialize(type: Type, obj: RandomizedValue?, node: ConfigurationNode) {
        // throws if no value is provided
        obj ?: throw SerializationException()

        with(obj) {
            if (!isScaled && !isRandom) {
                // case 1
                node.set(base)
            } else {
                // case 2/3/4
                with(node) {
                    node("base").set(base)
                    if (isScaled) {
                        // if scale is set
                        node("scale").set(scale)
                    }
                    if (isRandom) {
                        // if spread is set
                        node("spread").set(sigma)
                        if (isLowerBounded) {
                            // if lower bound is set
                            node("min").set(lowerBound)
                        }
                        if (isUpperBounded) {
                            // if upper bound is set
                            node("max").set(upperBound)
                        }
                    }
                }
            }
        }
    }
}

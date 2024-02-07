package cc.mewcraft.wakame.util

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type
import java.text.DecimalFormat
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
data class NumericValue(
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
     * Whatever, this constrains to a minimum and maximum of output.
     */
    val threshold: Double? = null,
) {

    /**
     * Whether the numeric value has scale enabled or not.
     */
    val isScaled: Boolean
        get() = scale > .0

    /**
     * Whether the numeric value has random enabled or not.
     */
    val isRandom: Boolean
        get() = sigma > .0

    /**
     * Whether the random value is bounded by the [threshold].
     */
    val isBounded: Boolean
        get() = threshold != null

    private constructor(base: Number, scale: Number = .0, sigma: Number = .0, threshold: Number? = null) : this(
        base.toDouble(), scale.toDouble(), sigma.toDouble(), threshold?.toDouble()
    )

    init {
        check(scale >= 0) { "scale must not be negative" }
        check(sigma >= 0) { "sigma must not be negative" }
        if (threshold != null) {
            check(threshold >= 0) { "threshold must not be negative" }
            check(threshold >= sigma) { "threshold must be greater or equal to sigma" }
        }
    }

    companion object Factory {
        private val DECIMAL_FORMAT: DecimalFormat = DecimalFormat("0.####")
        private val CONFIG_SERIALIZER: TypeSerializer<NumericValue> = NumericValueSerializer()

        /**
         * Creates an instance from a string.
         *
         * @param value a string in the format "base{,scale{,sigma{,threshold}}}",
         *     where the values inside curly brackets can be omitted
         * @return an instance
         */
        fun create(value: String): NumericValue {
            val split = value.split(",").dropLastWhile { it.isEmpty() }.toTypedArray()
            val base = split[0].toDouble()
            val scale = if (split.size > 1) split[1].toDouble() else 0.0
            val sigma = if (split.size > 2) split[2].toDouble() else 0.0
            val threshold = if (split.size > 3) split[3].toDouble() else 0.0
            return NumericValue(base, scale, sigma, threshold)
        }

        /**
         * Creates an instance from a single number.
         *
         * @param base a base value
         * @return an instance
         */
        fun create(base: Number): NumericValue {
            return NumericValue(base)
        }

        /**
         * Creates an instance from a [configuration node][ConfigurationNode].
         *
         * @param node a configuration node, see the implementation of
         *     [ConfigSerializer] for the supported structures
         * @return an instance
         */
        fun create(node: ConfigurationNode): NumericValue {
            return CONFIG_SERIALIZER.deserialize(NumericValue::class.java, node)
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
    fun calculate(scalingFactor: Double = .0, randomVariable: Double = ThreadLocalRandom.current().nextGaussian()): Double {
        // The mean (mu), the center of the distribution
        val scaledBase = base + (scale * scalingFactor)

        /*
           According to Z-score formula:
                z = (x - mu) / sigma
           where
                z is Z-score,
                x is standardized value
                mu is mean (base)
                sigma is standard deviation
           and
                x is the value before standardization,
                also it's the value we want to calculate.

           Transform the formula, we get:
                x = z * sigma + mu
           Note that we already know these values:
                z, sigma, mu
        */

        // Calculate "z * sigma", where we call it "spread"
        val spread = if (threshold != null) {
            (randomVariable * sigma).coerceIn(-threshold, threshold)
            // ^z-score        ^sigma          ^min spread ^max spread
        } else {
            (randomVariable * sigma).coerceIn(-sigma * 2, sigma * 2)
            // ^z-score        ^sigma          ^min spread ^max spread
        }

        // Since the mean (mu) is already scaled,
        // we can't simply do `x = z * sigma + mu`.
        // Instead, we calculate the relative value:
        return scaledBase * (1 + spread)
    }

    /**
     * Calculates the value.
     *
     * This function is the same as [calculate] but different in that it
     * accepts abstract [Numbers][Number] instead of only [Double]. The
     * given [Numbers][Number] values are converted to [Double] values using
     * [Number.toDouble].
     */
    fun calculate(scalingFactor: Number = .0, randomVariable: Number = ThreadLocalRandom.current().nextGaussian()): Double {
        return calculate(scalingFactor = scalingFactor.toDouble(), randomVariable = randomVariable.toDouble())
    }

    /**
     * Calculates the value.
     *
     * This is equivalent to calling [calculate] with [scaling factor] **zero**
     * and a random variable from the standard normal distribution.
     */
    fun calculate(): Double {
        return calculate(.0)
    }
}

class NumericValueSerializer : TypeSerializer<NumericValue> {
    override fun deserialize(type: Type, node: ConfigurationNode): NumericValue {
        val scalar = node.rawScalar()
        if (scalar != null) {
            // if it's just a simple plain value like "value: 32"
            return NumericValue(node.double, .0, .0, .0)
        }

        val base = node.node("base").apply { require(Double::class.java) }.double
        val scale = node.node("scale").takeIf { !it.virtual() }?.apply { require(Double::class.java) }?.double
        val sigma = node.node("spread").takeIf { !it.virtual() }?.apply { require(Double::class.java) }?.double
        val threshold = node.node("max").takeIf { !it.virtual() }?.apply { require(Double::class.java) }?.double

        return NumericValue(base, scale ?: .0, sigma ?: .0, threshold)
    }

    override fun serialize(type: Type, obj: NumericValue?, node: ConfigurationNode) {
        // throws if no value is provided
        obj ?: throw SerializationException()

        /*
           Possible config structures:

           Case 1: (only base)
               ```
               node: <base>
               ```
           Case 2: (base + scale)
               ```
               node:
                   base: <base>
                   scale: <scale>
               ```
           Case 3: (base + normal dist)
               ```
               node:
                   base: <base>
                   spread: <sigma>
                   max: <threshold>
               ```
           Case 4: (base + scale + normal dist)
               ```
               node:
                   base: <base>
                   scale: <scale>
                   spread: <sigma>
                   max: <threshold>
               ```
        */

        with(obj) {

            // intentionally write the redundant boolean logic

            if (!isScaled && !isRandom) {
                // case 1
                node.set(base)
            } else {
                // case 2/3/4
                with(node) {
                    node("base").set(base)
                    if (isScaled) {
                        // if scale is on
                        node("scale").set(scale)
                    }
                    if (isRandom) {
                        // if random is on
                        node("spread").set(sigma)
                        if (isBounded) {
                            // if threshold is set
                            node("max").set(threshold)
                        }
                    }
                }
            }
        }
    }
}

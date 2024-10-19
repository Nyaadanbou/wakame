@file:Suppress("MemberVisibilityCanBePrivate")

package cc.mewcraft.wakame.attribute

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.intellij.lang.annotations.Pattern
import java.util.stream.Stream

/**
 * An attribute type with a numerical default value.
 *
 * ## Notes to users of the code
 *
 * By design, you should not create the instance yourself because the objects
 * are generally used as conceptual types. Instead, use the singleton [Attributes]
 * to get the instances. The same also applies to its subtypes.
 *
 * @property compositeId
 * @property descriptionId 属性的唯一标识
 * @property defaultValue 属性的默认数值 (非 [Provider])
 * @property vanilla 属性是否由原版属性实现
 *
 * @see RangedAttribute
 * @see ElementAttribute
 */
open class SimpleAttribute
/**
 * @param defaultValue 属性的默认数值 ([Provider])
 */
protected constructor(
    @Pattern(AttributeSupport.ATTRIBUTE_ID_PATTERN_STRING)
    final override val compositeId: String,
    @Pattern(AttributeSupport.ATTRIBUTE_ID_PATTERN_STRING)
    final override val descriptionId: String,
    defaultValue: Provider<Double>,
    final override val vanilla: Boolean = false,
) : Examinable, Attribute {

    // 需要注意 (kotlin 委托基础)
    // 当一个 property 将值委托给 provider 时，其返回的是 provider 的值，而不是 provider
    // 也就是说，从 defaultValue 拿到的值它是常量，是不会自动更新的
    // 或者，你也可以简单的理解成因为 double 是常量，所以不会自动更新
    //
    // 所以说，当你想要 AttributeInstance#getBaseValue 的值能自动更新，
    // 你应该直接返回 Attribute#defaultValue 的值，而不是赋值给其他 val 然后返回那个 val
    // 这也意味着，我们也不需要再写一个 defaultValueProvider 的 property
    //
    // 这里说的同样也适用于该文件其他用到 `by` 的地方
    final override val defaultValue: Double by defaultValue

    /**
     * Instantiates the type using the global attribute config as value providers.
     *
     * This constructor is used if the [compositeId] is different from the [descriptionId].
     *
     * @param compositeId the ID of the composite attribute to which this attribute is related
     */
    internal constructor(
        compositeId: String,
        descriptionId: String,
        defaultValue: Double,
        vanilla: Boolean = false,
    ) : this(
        compositeId = compositeId,
        descriptionId = descriptionId,
        defaultValue = AttributeSupport.GLOBAL_ATTRIBUTE_CONFIG.optionalEntry<Double>(compositeId, "values", "default").orElse(defaultValue),
        vanilla = vanilla    )

    internal constructor(
        descriptionId: String,
        defaultValue: Double,
        vanilla: Boolean = false,
    ) : this(
        compositeId = descriptionId,
        descriptionId = descriptionId,
        defaultValue = defaultValue,
        vanilla = vanilla,
    )

    override fun sanitizeValue(value: Double): Double {
        return value
    }

    override val key: Key = Key.key(Namespaces.ATTRIBUTE, descriptionId)

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("descriptionId", descriptionId),
            ExaminableProperty.of("defaultValue", defaultValue),
            ExaminableProperty.of("vanilla", vanilla),
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other is Attribute)
            return descriptionId == other.descriptionId
        return false
    }

    override fun hashCode(): Int {
        return descriptionId.hashCode()
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

/**
 * An [Attribute] type with bounded values.
 *
 * The [minValue] and [maxValue] put a threshold on the final value of this attribute
 * after all [AttributeModifier]s have been applied.
 */
open class RangedAttribute
/**
 * @param minValue 该属性允许的最小数值（[Provider]）
 * @param maxValue 该属性允许的最大数值（[Provider]）
 */
protected constructor(
    compositeId: String,
    descriptionId: String,
    defaultValue: Provider<Double>,
    minValue: Provider<Double>,
    maxValue: Provider<Double>,
    vanilla: Boolean = false,
) : SimpleAttribute(compositeId, descriptionId, defaultValue, vanilla) {
    val minValue: Double by minValue
    val maxValue: Double by maxValue

    /**
     * Instantiates the type using the global attribute config as value providers.
     *
     * This constructor is used if the [compositeId] is different from the [descriptionId].
     *
     * @param compositeId the ID of the composite attribute to which this attribute is related
     */
    internal constructor(
        compositeId: String,
        descriptionId: String,
        defaultValue: Double,
        minValue: Double,
        maxValue: Double,
        vanilla: Boolean = false,
    ) : this(
        compositeId = compositeId,
        descriptionId = descriptionId,
        defaultValue = AttributeSupport.GLOBAL_ATTRIBUTE_CONFIG.optionalEntry<Double>(compositeId, "values", "default").orElse(defaultValue),
        minValue = AttributeSupport.GLOBAL_ATTRIBUTE_CONFIG.optionalEntry<Double>(compositeId, "values", "min").orElse(minValue),
        maxValue = AttributeSupport.GLOBAL_ATTRIBUTE_CONFIG.optionalEntry<Double>(compositeId, "values", "max").orElse(maxValue),
        vanilla = vanilla
    )

    internal constructor(
        descriptionId: String,
        defaultValue: Double,
        minValue: Double,
        maxValue: Double,
        vanilla: Boolean = false,
    ) : this(
        compositeId = descriptionId,
        descriptionId = descriptionId,
        defaultValue = defaultValue,
        minValue = minValue,
        maxValue = maxValue,
        vanilla = vanilla
    )

    init {
        if (this.minValue > this.maxValue) {
            throw IllegalArgumentException("Minimum value cannot be higher than maximum value!")
        } else if (this.defaultValue < this.minValue) {
            throw IllegalArgumentException("Default value cannot be lower than minimum value!")
        } else if (this.defaultValue > this.maxValue) {
            throw IllegalArgumentException("Default value cannot be higher than maximum value!")
        }
    }

    override fun sanitizeValue(value: Double): Double {
        return if (value.isNaN()) {
            minValue
        } else {
            value.coerceIn(minValue, maxValue)
        }
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.concat(
            super.examinableProperties(),
            Stream.of(
                ExaminableProperty.of("minValue", minValue),
                ExaminableProperty.of("maxValue", maxValue),
            ),
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other is RangedAttribute)
            return descriptionId == other.descriptionId /* && element == other.element */
        return false
    }

    override fun hashCode(): Int {
        val result = descriptionId.hashCode()
        // result = (31 * result) + element.hashCode()
        return result
    }
}

/**
 * An [Attribute] type with an [Element].
 */
open class ElementAttribute
protected constructor(
    compositeId: String,
    descriptionId: String,
    defaultValue: Provider<Double>,
    minValue: Provider<Double>,
    maxValue: Provider<Double>,
    val element: Element,
    vanilla: Boolean = false,
) : RangedAttribute(
    compositeId,
    descriptionId + "/" + element.uniqueId,
    defaultValue,
    minValue,
    maxValue,
    vanilla,
) {

    /**
     * Instantiates the type using the global attribute config as value providers.
     *
     * This constructor is used if the [compositeId] is different from the [descriptionId].
     *
     * @param compositeId the ID of the composite attribute to which this attribute is related
     */
    internal constructor(
        compositeId: String,
        descriptionId: String,
        defaultValue: Double,
        minValue: Double,
        maxValue: Double,
        element: Element,
        vanilla: Boolean = false,
    ) : this(
        compositeId = compositeId,
        descriptionId = descriptionId,
        defaultValue = AttributeSupport.GLOBAL_ATTRIBUTE_CONFIG.optionalEntry<Double>(compositeId, "values", element.uniqueId, "default").orElse(defaultValue),
        minValue = AttributeSupport.GLOBAL_ATTRIBUTE_CONFIG.optionalEntry<Double>(compositeId, "values", element.uniqueId, "min").orElse(minValue),
        maxValue = AttributeSupport.GLOBAL_ATTRIBUTE_CONFIG.optionalEntry<Double>(compositeId, "values", element.uniqueId, "max").orElse(maxValue),
        element = element,
        vanilla = vanilla
    )

    internal constructor(
        descriptionId: String,
        defaultValue: Double,
        minValue: Double,
        maxValue: Double,
        element: Element,
        vanilla: Boolean = false,
    ) : this(
        compositeId = descriptionId,
        descriptionId = descriptionId,
        defaultValue = defaultValue,
        minValue = minValue,
        maxValue = maxValue,
        element = element,
        vanilla = vanilla
    )

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.concat(
            super.examinableProperties(),
            Stream.of(
                ExaminableProperty.of("element", element.uniqueId)
            )
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other is ElementAttribute)
            return descriptionId == other.descriptionId /* && element == other.element */
        return false
    }

    override fun hashCode(): Int {
        val result = descriptionId.hashCode()
        // result = (31 * result) + element.hashCode()
        return result
    }
}

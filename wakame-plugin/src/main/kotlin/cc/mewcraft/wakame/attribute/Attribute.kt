@file:Suppress("MemberVisibilityCanBePrivate")

package cc.mewcraft.wakame.attribute

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.commons.provider.immutable.provider
import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.config.*
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registry.ATTRIBUTE_CONFIG_FILE
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.intellij.lang.annotations.Pattern
import java.util.stream.Stream

private val ATTRIBUTE_CONFIG by lazy { Configs.YAML[ATTRIBUTE_CONFIG_FILE] }

/**
 * An identifiable numerical value.
 *
 * By design, you should not create the instance yourself. Instead, use the
 * singleton [Attributes] to get your expected instances. In most cases,
 * instances of this class (and its subclasses, too) are solely used as
 * references. The property values (such as [defaultValue]) do not matter
 * for the code outside of this package.
 *
 * @see RangedAttribute
 * @see ElementAttribute
 */
open class Attribute @InternalApi constructor(
    /**
     * 属性的唯一标识。
     */
    @Pattern("[a-z0-9_.]")
    val descriptionId: String,
    /**
     * 属性是否由原版属性实现。
     */
    val vanilla: Boolean = false,
    /**
     * 属性的默认数值 [Provider], 重载时会更新数值。
     */
    val defaultValueProvider: Provider<Double>,
) : Keyed, Examinable {
    override val key: Key = Key(Namespaces.ATTRIBUTE, descriptionId)

    /**
     * 属性的默认数值, 获取的值不会随着重载而更新。
     */
    val defaultValue: Double by defaultValueProvider

    /**
     * 清理给定的数值，使其落在该属性的合理数值范围内。
     *
     * @param value 要清理的数值
     * @return 清理好的数值
     */
    open fun sanitizeValue(value: Double): Double {
        return value
    }

    override fun hashCode(): Int {
        return descriptionId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is Attribute) return descriptionId == other.descriptionId
        return false
    }

    override fun toString(): String {
        return toSimpleString()
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("descriptionId", descriptionId),
        ExaminableProperty.of("defaultValue", defaultValue),
        ExaminableProperty.of("vanilla", vanilla),
    )
}

@InternalApi
fun Attribute(
    descriptionId: String,
    vanilla: Boolean = false,
    defaultValue: Double
): Attribute {
    return Attribute(descriptionId, vanilla, provider(defaultValue))
}

/**
 * An [Attribute] with bounded values.
 */
@OptIn(InternalApi::class)
open class RangedAttribute
@InternalApi
constructor(
    descriptionId: String,
    vanilla: Boolean,
    defaultValue: Provider<Double>,
    /**
     * 该属性允许的最小数值, 重载时会更新数值。
     */
    val minValueProvider: Provider<Double>,
    /**
     * 该属性允许的最大数值, 重载时会更新数值。
     */
    val maxValueProvider: Provider<Double>,
) : Attribute(descriptionId, vanilla, defaultValue) {
    /**
     * 该属性允许的最小数值, 获取的值不会随着重载而更新。
     */
    val minValue: Double by minValueProvider

    /**
     * 该属性允许的最大数值, 获取的值不会随着重载而更新。
     */
    val maxValue: Double by maxValueProvider

    init {
        if (this.minValue > this.maxValue) {
            throw IllegalArgumentException("Minimum value cannot be bigger than maximum value!")
        } else if (this.defaultValue < this.minValue) {
            throw IllegalArgumentException("Default value cannot be lower than minimum value!")
        } else if (this.defaultValue > this.maxValue) {
            throw IllegalArgumentException("Default value cannot be bigger than maximum value!")
        }
    }

    override fun sanitizeValue(value: Double): Double {
        return if (value.isNaN()) {
            minValue
        } else {
            value.coerceIn(minValue, maxValue)
        }
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.concat(
        super.examinableProperties(),
        Stream.of(
            ExaminableProperty.of("minValue", minValue),
            ExaminableProperty.of("maxValue", maxValue),
        ),
    )
}

@InternalApi
fun RangedAttribute(
    descriptionId: String,
    vanilla: Boolean,
    defaultValue: Double,
    minValue: Double,
    maxValue: Double
): RangedAttribute {
    return RangedAttribute(
        descriptionId,
        vanilla,
        ATTRIBUTE_CONFIG.optionalEntry<Double>(descriptionId, "default_value").orElse(defaultValue),
        ATTRIBUTE_CONFIG.optionalEntry<Double>(descriptionId, "min_value").orElse(minValue),
        ATTRIBUTE_CONFIG.optionalEntry<Double>(descriptionId, "max_value").orElse(maxValue)
    )
}

@InternalApi
fun RangedAttribute(
    descriptionId: String,
    defaultValue: Double,
    minValue: Double,
    maxValue: Double
): RangedAttribute {
    return RangedAttribute(descriptionId, false, defaultValue, minValue, maxValue)
}

/**
 * An [Attribute] related to an [Element].
 */
@OptIn(InternalApi::class)
open class ElementAttribute
@InternalApi
constructor(
    descriptionId: String,
    vanilla: Boolean,
    defaultValue: Provider<Double>,
    minValue: Provider<Double>,
    maxValue: Provider<Double>,
    /**
     * 该属性所关联的元素种类。
     */
    val element: Element,
) : RangedAttribute(
    descriptionId,
    vanilla,
    defaultValue,
    minValue,
    maxValue,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is ElementAttribute) return descriptionId == other.descriptionId && element == other.element
        return false
    }

    override fun hashCode(): Int {
        var result = descriptionId.hashCode()
        result = (31 * result) + element.hashCode()
        return result
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.concat(
        super.examinableProperties(),
        Stream.of(
            ExaminableProperty.of("element", element),
        )
    )
}

@InternalApi
fun ElementAttribute(
    descriptionId: String,
    vanilla: Boolean,
    defaultValue: Double,
    minValue: Double,
    maxValue: Double,
    element: Element
): ElementAttribute {
    return ElementAttribute(
        descriptionId,
        vanilla,
        ATTRIBUTE_CONFIG.optionalEntry<Double>(descriptionId, element.uniqueId, "default_value").orElse(defaultValue),
        ATTRIBUTE_CONFIG.optionalEntry<Double>(descriptionId, element.uniqueId, "min_value").orElse(minValue),
        ATTRIBUTE_CONFIG.optionalEntry<Double>(descriptionId, element.uniqueId, "max_value").orElse(maxValue),
        element
    )
}

@InternalApi
fun ElementAttribute(
    descriptionId: String,
    defaultValue: Double,
    minValue: Double,
    maxValue: Double,
    element: Element
): ElementAttribute {
    return ElementAttribute(descriptionId, false, defaultValue, minValue, maxValue, element)
}

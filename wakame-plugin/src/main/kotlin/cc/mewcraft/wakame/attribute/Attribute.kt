@file:Suppress("MemberVisibilityCanBePrivate")

package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.intellij.lang.annotations.Pattern
import java.util.stream.Stream

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
     * 属性的默认数值。
     */
    val defaultValue: Double,
    /**
     * 属性是否由原版属性实现。
     */
    val vanilla: Boolean = false,
) : Keyed, Examinable {
    override val key: Key = Key(Namespaces.ATTRIBUTE, descriptionId)

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

/**
 * An [Attribute] with bounded values.
 */
@OptIn(InternalApi::class)
open class RangedAttribute
@InternalApi
constructor(
    descriptionId: String,
    defaultValue: Double,
    vanilla: Boolean,
    /**
     * 该属性允许的最小数值。
     */
    val minValue: Double,
    /**
     * 该属性允许的最大数值。
     */
    val maxValue: Double,
) : Attribute(descriptionId, defaultValue, vanilla) {
    @InternalApi
    constructor(
        descriptionId: String,
        defaultValue: Double,
        minValue: Double,
        maxValue: Double,
    ) : this(descriptionId, defaultValue, false, minValue, maxValue)

    init {
        if (minValue > maxValue) {
            throw IllegalArgumentException("Minimum value cannot be bigger than maximum value!")
        } else if (defaultValue < minValue) {
            throw IllegalArgumentException("Default value cannot be lower than minimum value!")
        } else if (defaultValue > maxValue) {
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

/**
 * An [Attribute] related to an [Element].
 */
@OptIn(InternalApi::class)
open class ElementAttribute
@InternalApi
constructor(
    descriptionId: String,
    defaultValue: Double,
    vanilla: Boolean,
    minValue: Double,
    maxValue: Double,
    /**
     * 该属性所关联的元素种类。
     */
    val element: Element,
) : RangedAttribute(
    descriptionId,
    defaultValue,
    vanilla,
    minValue,
    maxValue,
) {
    @InternalApi
    constructor(
        descriptionId: String,
        defaultValue: Double,
        minValue: Double,
        maxValue: Double,
        element: Element,
    ) : this(
        descriptionId, defaultValue, false, minValue, maxValue, element
    )

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
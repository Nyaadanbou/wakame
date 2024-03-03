@file:Suppress("MemberVisibilityCanBePrivate")

package cc.mewcraft.wakame.attribute.base

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.element.Element
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import net.kyori.examination.string.StringExaminer
import org.intellij.lang.annotations.Pattern

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
) : Keyed {
    /**
     * 清理给定的数值，使其落在该属性的合理数值范围内。
     *
     * @param value 要清理的数值
     * @return 清理好的数值
     */
    open fun sanitizeValue(value: Double): Double {
        return value
    }

    override fun key(): Key {
        return Key.key(NekoNamespaces.ATTRIBUTE, descriptionId)
    }

    override fun hashCode(): Int {
        return descriptionId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Attribute) return false
        return descriptionId == other.descriptionId
    }

    override fun toString(): String {
        return key().examine(StringExaminer.simpleEscaping())
    }
}

/**
 * An [Attribute] with bounded values.
 */
@OptIn(InternalApi::class)
open class RangedAttribute @InternalApi constructor(
    descriptionId: String,
    defaultValue: Double,
    /**
     * 该属性允许的最小数值。
     */
    val minValue: Double,
    /**
     * 该属性允许的最大数值。
     */
    val maxValue: Double,
) : Attribute(descriptionId, defaultValue) {
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
}

/**
 * An [Attribute] related to an [Element].
 */
@OptIn(InternalApi::class)
open class ElementAttribute @InternalApi constructor(
    /**
     * 该属性所关联的元素种类。
     */
    val element: Element,
    descriptionId: String,
    defaultValue: Double,
    minValue: Double,
    maxValue: Double,
) : RangedAttribute(
    descriptionId,
    defaultValue,
    minValue,
    maxValue
)
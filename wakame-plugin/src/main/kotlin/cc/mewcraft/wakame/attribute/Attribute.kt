package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.element.Element
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import net.kyori.examination.string.StringExaminer
import org.intellij.lang.annotations.Pattern

/**
 * A numerical value related to an entity such as a player or monster.
 *
 * This is also used to store an arbitrary value of an [Attribute]
 * type, which is often used in conjunction with the singleton object
 * [Attributes].
 *
 * For example, you can create a map, where the map keys are **constant**
 * instances from the singleton [Attributes] such as [Attributes.DEFENSE]
 * and [Attributes.MAX_HEALTH], and the map values are newly created
 * instances of [Attribute] by your own.
 *
 * @see RangedAttribute
 * @see AttributeModifier
 * @see AttributeInstance
 */
open class Attribute(
    /**
     * 属性的唯一标识。
     */
    @Pattern("[a-z_]")
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
        return Key.key("attribute", descriptionId)
    }

    override fun toString(): String {
        return key().examine(StringExaminer.simpleEscaping())
    }
}

/**
 * An [Attribute] with bounded values.
 */
open class RangedAttribute(
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
 * An [Attribute] related to an element.
 */
open class ElementAttribute(
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
package cc.mewcraft.wakame.attribute.facade

import cc.mewcraft.wakame.attribute.base.AttributeModifier.Operation
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.BinaryCoreValue
import cc.mewcraft.wakame.registry.AttributeStructMeta
import cc.mewcraft.wakame.registry.AttributeStructType

/**
 * 代表一个属性在 NBT 中的数据。
 *
 * 一个经典的使用场景就是高频率传递属性的数据。较好的实现是不创建新对象来传递数值，而是修改已有实例的属性进行传递。
 *
 * 实现类应该将成员变量声明为 `var` 而不是 `val` 以实现复用。同时配合 [ThreadLocal] 高效解决并发问题。
 */
sealed interface BinaryAttributeValue : BinaryCoreValue {
    var operation: Operation
    val structType: AttributeStructType
}

/**
 * Represents a "single-value" attribute.
 */
interface BinaryAttributeValueSingle : BinaryAttributeValue {
    var value: Double
}

/**
 * Represents a "ranged-value" attribute.
 */
interface BinaryAttributeValueRanged : BinaryAttributeValue {
    var lower: Double
    var upper: Double
}

/**
 * Represents an "elemental" attribute.
 */
interface BinaryAttributeValueElement : BinaryAttributeValue {
    var element: Element
}

/**
 * 快速查询属性数据的格式。
 */
val BinaryAttributeValue.format: AttributeStructMeta.Format
    get() = when (this) {
        is BinaryAttributeValueSingle -> AttributeStructMeta.Format.SINGLE
        is BinaryAttributeValueRanged -> AttributeStructMeta.Format.RANGED
        else -> error("Unhandled format. Missing implementation?")
    }

/**
 * 快速查询属性数据是否带元素。
 */
val BinaryAttributeValue.elemental: Boolean
    get() = this is BinaryAttributeValueElement

/**
 * 快速获得属性数据的元素类型。
 */
val BinaryAttributeValue.elementOrNull: Element?
    get() = (this as? BinaryAttributeValueElement)?.element

//<editor-fold desc="Implementations">
/* 以下实现考虑了属性可能拥有的全部数据结构 */

data class BinaryAttributeValueS(
    override var value: Double,
    override var operation: Operation,
) : BinaryAttributeValueSingle {
    override val structType: AttributeStructType = AttributeStructType.SINGLE
}

data class BinaryAttributeValueLU(
    override var lower: Double,
    override var upper: Double,
    override var operation: Operation,
) : BinaryAttributeValueRanged {
    override val structType: AttributeStructType = AttributeStructType.RANGED
}

data class BinaryAttributeValueSE(
    override var value: Double,
    override var element: Element,
    override var operation: Operation,
) : BinaryAttributeValueSingle, BinaryAttributeValueElement {
    override val structType: AttributeStructType = AttributeStructType.SINGLE_ELEMENT
}

data class BinaryAttributeValueLUE(
    override var lower: Double,
    override var upper: Double,
    override var element: Element,
    override var operation: Operation,
) : BinaryAttributeValueRanged, BinaryAttributeValueElement {
    override val structType: AttributeStructType = AttributeStructType.RANGED_ELEMENT
}
//</editor-fold>

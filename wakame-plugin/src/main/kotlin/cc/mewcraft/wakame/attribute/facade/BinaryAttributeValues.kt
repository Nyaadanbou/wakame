package cc.mewcraft.wakame.attribute.facade

import cc.mewcraft.wakame.attribute.base.AttributeModifier
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
    var operation: AttributeModifier.Operation
    val structType: AttributeStructType
}

/**
 * Represents a "single-value" attribute.
 */
interface BinaryAttributeValueSingle<T> : BinaryAttributeValue {
    var value: T
}

/**
 * Represents a "ranged-value" attribute.
 */
interface BinaryAttributeValueRanged<T> : BinaryAttributeValue {
    var lower: T
    var upper: T
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
internal val BinaryAttributeValue.format: AttributeStructMeta.Format
    get() = when (this) {
        is BinaryAttributeValueSingle<*> -> AttributeStructMeta.Format.SINGLE
        is BinaryAttributeValueRanged<*> -> AttributeStructMeta.Format.RANGED
        else -> error("Unknown format")
    }

/**
 * 快速查询属性数据是否带元素。
 */
internal val BinaryAttributeValue.elemental: Boolean
    get() = this is BinaryAttributeValueElement

/**
 * 快速获得属性数据的元素类型。
 */
internal val BinaryAttributeValue.elementOrNull: Element?
    get() = (this as? BinaryAttributeValueElement)?.element

////// 以下实现考虑了属性可能拥有的全部数据结构

data class BinaryAttributeValueS<T : Number>(
    override var value: T, override var operation: AttributeModifier.Operation,
) : BinaryAttributeValueSingle<T> {
    override val structType: AttributeStructType = AttributeStructType.SINGLE
}

data class BinaryAttributeValueLU<T : Number>(
    override var lower: T, override var upper: T, override var operation: AttributeModifier.Operation,
) : BinaryAttributeValueRanged<T> {
    override val structType: AttributeStructType = AttributeStructType.RANGED
}

data class BinaryAttributeValueSE<T : Number>(
    override var value: T, override var element: Element, override var operation: AttributeModifier.Operation,
) : BinaryAttributeValueSingle<T>, BinaryAttributeValueElement {
    override val structType: AttributeStructType = AttributeStructType.SINGLE_ELEMENT
}

data class BinaryAttributeValueLUE<T : Number>(
    override var lower: T, override var upper: T, override var element: Element, override var operation: AttributeModifier.Operation,
) : BinaryAttributeValueRanged<T>, BinaryAttributeValueElement {
    override val structType: AttributeStructType = AttributeStructType.RANGED_ELEMENT
}

package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.BinaryCoreValue

/**
 * 代表一个属性在 NBT 中的数据。
 *
 * 一个经典的使用场景就是高频率传递属性的数据。较好的实现是不创建新对象来传递数值，而是修改已有实例的属性进行传递。
 *
 * 实现类应该将成员变量声明为 `var` 而不是 `val` 以实现复用。同时配合 [ThreadLocal] 高效解决并发问题。
 */
sealed interface BinaryAttributeValue : BinaryCoreValue

////// 以下实现考虑了属性可能拥有的全部数据结构

data class BinaryAttributeValueV<T>(
    // FIXME 也许根本不需要 generics
    var value: T, var operation: AttributeModifier.Operation,
) : BinaryAttributeValue

data class BinaryAttributeValueLU<T>(
    var lower: T, var upper: T, var operation: AttributeModifier.Operation,
) : BinaryAttributeValue

data class BinaryAttributeValueVE<T>(
    var value: T, var element: Element, var operation: AttributeModifier.Operation,
) : BinaryAttributeValue

data class BinaryAttributeValueLUE<T>(
    var lower: T, var upper: T, var element: Element, var operation: AttributeModifier.Operation,
) : BinaryAttributeValue

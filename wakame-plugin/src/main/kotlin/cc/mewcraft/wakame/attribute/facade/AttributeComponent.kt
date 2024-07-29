package cc.mewcraft.wakame.attribute.facade

import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.element.Element as ElementType

/*
   属性数据的抽象。

   这些类描述了"属性组件"的所有可能的结构，详细用途参见实现。
*/

/**
 * Represents possible components of the attribute values.
 *
 * These interfaces serve as clarifying class hierarchy. You should
 * not directly implement these interfaces. Check the subtypes of
 * [AttributeData] for details.
 */
sealed interface AttributeComponent {
    /**
     * The `operation` component.
     */
    interface Op : AttributeComponent {
        val operation: AttributeModifier.Operation
    }

    /**
     * The `element` component.
     */
    interface Element : AttributeComponent {
        val element: ElementType
    }

    /**
     * The `fixed` value component.
     */
    interface Fixed<T> : AttributeComponent {
        val value: T
    }

    /**
     * The `ranged` value component.
     */
    interface Ranged<T> : AttributeComponent {
        val lower: T
        val upper: T
    }
}

/**
 * A mark interface for clarifying class hierarchy.
 *
 * **Do not directly implement this interface!**
 *
 * Instead, implement the following:
 * - [AttributeComponentS]
 * - [AttributeComponentR]
 * - [AttributeComponentSE]
 * - [AttributeComponentRE]
 */
interface AttributeData

/**
 * Components: [AttributeComponent.Op], [AttributeComponent.Fixed]
 */
interface AttributeComponentS<T> :
    AttributeData,
    AttributeComponent.Op,
    AttributeComponent.Fixed<T> {

    override val operation: AttributeModifier.Operation
    override val value: T
}

/**
 * Components: [AttributeComponent.Op], [AttributeComponent.Fixed]
 */
interface AttributeComponentR<T> :
    AttributeData,
    AttributeComponent.Op,
    AttributeComponent.Ranged<T> {

    override val operation: AttributeModifier.Operation
    override val lower: T
    override val upper: T
}

/**
 * Components: [AttributeComponent.Op], [AttributeComponent.Fixed], [AttributeComponent.Element]
 */
interface AttributeComponentSE<T> :
    AttributeData,
    AttributeComponent.Op,
    AttributeComponent.Fixed<T>,
    AttributeComponent.Element {

    override val operation: AttributeModifier.Operation
    override val value: T
    override val element: Element
}

/**
 * Components: [AttributeComponent.Op], [AttributeComponent.Ranged], [AttributeComponent.Element]
 */
interface AttributeComponentRE<T> :
    AttributeData,
    AttributeComponent.Op,
    AttributeComponent.Ranged<T>,
    AttributeComponent.Element {

    override val operation: AttributeModifier.Operation
    override val lower: T
    override val upper: T
    override val element: ElementType
}

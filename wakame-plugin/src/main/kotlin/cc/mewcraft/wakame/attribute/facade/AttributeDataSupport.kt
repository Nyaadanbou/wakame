package cc.mewcraft.wakame.attribute.facade

/*
   属性数据抽象。

   这些类描述了属性数值的所有可能的结构，详细用途参见实现。
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
    interface Op<OP> : AttributeComponent {
        val operation: OP
    }

    /**
     * The `element` component.
     */
    interface Element<E> : AttributeComponent {
        val element: E
    }

    /**
     * The `single` value component.
     */
    interface Single<S> : AttributeComponent {
        val value: S
    }

    /**
     * The `ranged` value component.
     */
    interface Ranged<R> : AttributeComponent {
        val lower: R
        val upper: R
    }
}

/**
 * A mark interface for clarifying class hierarchy.
 *
 * **Do not directly implement this interface!**
 *
 * Instead, implement the following:
 * - [AttributeDataS]
 * - [AttributeDataR]
 * - [AttributeDataSE]
 * - [AttributeDataRE]
 */
interface AttributeData

/**
 * Components: [AttributeComponent.Op], [AttributeComponent.Single]
 */
interface AttributeDataS<OP, S> :
    AttributeData,
    AttributeComponent.Op<OP>,
    AttributeComponent.Single<S> {

    override val operation: OP
    override val value: S
}

/**
 * Components: [AttributeComponent.Op], [AttributeComponent.Single]
 */
interface AttributeDataR<OP, R> :
    AttributeData,
    AttributeComponent.Op<OP>,
    AttributeComponent.Ranged<R> {

    override val operation: OP
    override val lower: R
    override val upper: R
}

/**
 * Components: [AttributeComponent.Op], [AttributeComponent.Single], [AttributeComponent.Element]
 */
interface AttributeDataSE<OP, S, E> :
    AttributeData,
    AttributeComponent.Op<OP>,
    AttributeComponent.Single<S>,
    AttributeComponent.Element<E> {

    override val operation: OP
    override val value: S
    override val element: E
}

/**
 * Components: [AttributeComponent.Op], [AttributeComponent.Ranged], [AttributeComponent.Element]
 */
interface AttributeDataRE<OP, R, E> :
    AttributeData,
    AttributeComponent.Op<OP>,
    AttributeComponent.Ranged<R>,
    AttributeComponent.Element<E> {

    override val operation: OP
    override val lower: R
    override val upper: R
    override val element: E
}

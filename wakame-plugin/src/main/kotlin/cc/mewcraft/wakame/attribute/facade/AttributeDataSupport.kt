package cc.mewcraft.wakame.attribute.facade

/*

属性数据抽象。

这些类描述了属性数值的所有可能的结构，详细用途参见实现。

*/

/**
 * Represents possible components of attribute values.
 *
 * These interfaces serve as clarifying class hierarchy. You should
 * not directly implement these interfaces. Check the subtypes of
 * [AttributeData] for details.
 */
sealed interface AttributeComponent {
    /**
     * The `operation` component.
     */
    interface Op<OP> {
        val operation: OP
    }

    /**
     * The `element` component.
     */
    interface Element<E> {
        val element: E
    }

    /**
     * The `single` value component.
     */
    interface Single<S> {
        val value: S
    }

    /**
     * The `ranged` value component.
     */
    interface Ranged<R> {
        val lower: R
        val upper: R
    }
}

/**
 * A mark interface for clarifying class hierarchy.
 *
 * **Do not implement this interface!**
 *
 * Instead, implement the following:
 * - [AttributeDataS]
 * - [AttributeDataR]
 * - [AttributeDataSE]
 * - [AttributeDataRE]
 */
sealed interface AttributeData

/**
 * Components: [AttributeComponent.Op], [AttributeComponent.Single]
 */
interface AttributeDataS<OP, S> : AttributeData,
    AttributeComponent.Op<OP>,
    AttributeComponent.Single<S> {

    override val operation: OP
    override val value: S
}

/**
 * Components: [AttributeComponent.Op], [AttributeComponent.Single]
 */
interface AttributeDataR<OP, R> : AttributeData,
    AttributeComponent.Op<OP>,
    AttributeComponent.Ranged<R> {

    override val operation: OP
    override val lower: R
    override val upper: R
}

/**
 * Components: [AttributeComponent.Op], [AttributeComponent.Single], [AttributeComponent.Element]
 */
interface AttributeDataSE<OP, S, E> : AttributeData,
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
interface AttributeDataRE<OP, R, E> : AttributeData,
    AttributeComponent.Op<OP>,
    AttributeComponent.Ranged<R>,
    AttributeComponent.Element<E> {

    override val operation: OP
    override val lower: R
    override val upper: R
    override val element: E
}

/*
   Below are top-level interfaces.

   You should not directly implement them.

   Instead, you create your own interfaces extending them,
   so that you can get rid of the generic parameters
   in your implementations and call sites.
*/

/**
 * An encoder.
 *
 * Data flow: [Arbitrary Object][E] -> [Attribute Value][V]
 */
interface AttributeDataEncoder<E, V : AttributeData> {
    fun encode(e: E): V
}

/**
 * A decoder.
 *
 * Data flow: [Attribute Value][V] -> [Arbitrary Object][E]
 */
interface AttributeDataDecoder<E, V : AttributeData> {
    fun decode(e: V): E
}

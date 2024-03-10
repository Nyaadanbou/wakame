package cc.mewcraft.wakame.attribute.facade

/*

属性数据抽象。

这些类描述了属性数值的所有结构，详细用途参见实现。

*/

/**
 * Represents the components of attribute values.
 *
 * **Do not directly implement these interfaces!**
 *
 * @see AttributeData and its subtypes
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
 * A mark interface for the purpose of clarifying hierarchy.
 *
 * **Do not implement this interface!**
 *
 * Implement the following instead:
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

/**
 * An encoder.
 *
 * Data flow: Arbitrary Object -> Attribute Value.
 */
interface AttributeDataEncoder<E, V : AttributeData> {
    fun encode(e: E): V
}

/**
 * A decoder.
 *
 * Data flow: Attribute Value -> Arbitrary Object
 */
interface AttributeDataDecoder<E, V : AttributeData> {
    fun decode(e: V): E
}

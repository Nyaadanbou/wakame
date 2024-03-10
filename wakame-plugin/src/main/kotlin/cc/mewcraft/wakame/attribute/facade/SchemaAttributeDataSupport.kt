package cc.mewcraft.wakame.attribute.facade

import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.SchemaCoreData
import cc.mewcraft.wakame.util.RandomizedValue
import org.spongepowered.configurate.ConfigurationNode

/*

属性数据抽象的实现，其数据类型为复合类型。

这些类用于储存属性的“模板数值”，也就是用于产生数值的数值。

*/

val SchemaAttributeData.element: Element?
    @Suppress("UNCHECKED_CAST")
    get() = (this as? AttributeComponent.Element<Element>)?.element

sealed interface SchemaAttributeData : SchemaCoreData, AttributeData, AttributeComponent.Op<Operation> {
    data class S(
        override val operation: Operation,
        override val value: RandomizedValue,
    ) : SchemaAttributeData, AttributeDataS<Operation, RandomizedValue>

    data class R(
        override val operation: Operation,
        override val lower: RandomizedValue,
        override val upper: RandomizedValue,
    ) : SchemaAttributeData, AttributeDataR<Operation, RandomizedValue>

    data class SE(
        override val operation: Operation,
        override val value: RandomizedValue,
        override val element: Element,
    ) : SchemaAttributeData, AttributeDataSE<Operation, RandomizedValue, Element>

    data class RE(
        override val operation: Operation,
        override val lower: RandomizedValue,
        override val upper: RandomizedValue,
        override val element: Element,
    ) : SchemaAttributeData, AttributeDataRE<Operation, RandomizedValue, Element>
}

/**
 * Attribute Value Encoder: [ConfigurationNode] -> [SchemaAttributeData].
 */
fun interface SchemaAttributeDataNodeEncoder : AttributeDataEncoder<ConfigurationNode, SchemaAttributeData>

/**
 * Attribute Value Encoder: [SchemaAttributeData] -> [ConfigurationNode].
 */
fun interface SchemaAttributeDataNodeDecoder : AttributeDataDecoder<ConfigurationNode, SchemaAttributeData>

package cc.mewcraft.wakame.attribute.facade

import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.schema.cell.core.attribute.SchemaAttributeCore
import cc.mewcraft.wakame.util.RandomizedValue
import org.spongepowered.configurate.ConfigurationNode

/*
   属性数据抽象的实现，其数据类型为复合类型。

   这些类用于储存属性的“模板数值”，也就是用于产生数值的数值。
*/

/**
 * Represents the data of **randomized** attribute values.
 */
sealed interface SchemaAttributeData {
    interface S : SchemaAttributeData, AttributeDataS<Operation, RandomizedValue>
    interface R : SchemaAttributeData, AttributeDataR<Operation, RandomizedValue>
    interface SE : SchemaAttributeData, AttributeDataSE<Operation, RandomizedValue, Element>
    interface RE : SchemaAttributeData, AttributeDataRE<Operation, RandomizedValue, Element>
}

/**
 * Data conversion: [ConfigurationNode] -> [SchemaAttributeCore].
 */
fun interface SchemaAttributeCoreNodeEncoder : AttributeDataEncoder<ConfigurationNode, SchemaAttributeCore>

/**
 * Data conversion: [SchemaAttributeCore] -> [ConfigurationNode].
 */
fun interface SchemaAttributeDataNodeDecoder : AttributeDataDecoder<ConfigurationNode, SchemaAttributeCore>

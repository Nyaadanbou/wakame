package cc.mewcraft.wakame.attribute.facade

import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.binary.cell.core.BinaryAttributeCore
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import org.spongepowered.configurate.ConfigurationNode

/*
   属性数据抽象的实现，其数据类型为初始类型。

   这些类充当 wakame 和游戏底层数据（NBT）之间的抽象层，
   以避免 wakame 的核心代码直接与游戏底层数据进行交互。

   这也方便我们给 wakame 编写单元测试，
   而不用在测试环境中构建一个真的 NBT。
*/

/**
 * Represents the data of **fixed** attribute values.
 */
sealed interface BinaryAttributeData : AttributeData {
    interface S : BinaryAttributeData, AttributeDataS<Operation, Double>
    interface R : BinaryAttributeData, AttributeDataR<Operation, Double>
    interface SE : BinaryAttributeData, AttributeDataSE<Operation, Double, Element>
    interface RE : BinaryAttributeData, AttributeDataRE<Operation, Double, Element>
}

/**
 * Data conversion: [ConfigurationNode] -> [BinaryAttributeCore].
 */
fun interface BinaryAttributeCoreNodeEncoder : AttributeDataEncoder<ConfigurationNode, BinaryAttributeCore>

/**
 * Data conversion: [CompoundShadowTag] -> [BinaryAttributeCore].
 */
fun interface BinaryAttributeCoreNbtEncoder : AttributeDataEncoder<CompoundShadowTag, BinaryAttributeCore>

/**
 * Data conversion: [BinaryAttributeCore] -> [CompoundShadowTag].
 */
fun interface BinaryAttributeCoreNbtDecoder : AttributeDataDecoder<CompoundShadowTag, BinaryAttributeCore>

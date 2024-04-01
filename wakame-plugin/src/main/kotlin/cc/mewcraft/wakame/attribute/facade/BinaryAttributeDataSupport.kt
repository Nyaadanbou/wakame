package cc.mewcraft.wakame.attribute.facade

import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.BinaryCoreData
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import org.spongepowered.configurate.ConfigurationNode

/*

属性数据抽象的实现，其数据类型为初始类型。

这些类充当 wakame 和游戏底层数据（NBT）之间的抽象层，
以避免 wakame 的核心代码直接与游戏底层数据进行交互。

这也方便我们给 wakame 编写单元测试，
而不用在测试环境中构建一个真的 NBT。

*/

val BinaryAttributeData.element: Element?
    @Suppress("UNCHECKED_CAST")
    get() = (this as? AttributeComponent.Element<Element>)?.element

/**
 * Represents the data of **fixed** attribute values.
 *
 * We especially extend [AttributeComponent.Op] as it's common among all subtypes.
 */
sealed interface BinaryAttributeData : AttributeData, AttributeComponent.Op<Operation>, BinaryCoreData {
    data class S(
        override val operation: Operation,
        override val value: Double,
    ) : BinaryAttributeData, AttributeDataS<Operation, Double>

    data class R(
        override val operation: Operation,
        override val lower: Double,
        override val upper: Double,
    ) : BinaryAttributeData, AttributeDataR<Operation, Double>

    data class SE(
        override val operation: Operation,
        override val value: Double,
        override val element: Element,
    ) : BinaryAttributeData, AttributeDataSE<Operation, Double, Element>

    data class RE(
        override val operation: Operation,
        override val lower: Double,
        override val upper: Double,
        override val element: Element,
    ) : BinaryAttributeData, AttributeDataRE<Operation, Double, Element>
}

/**
 * Data conversion: [ConfigurationNode] -> [BinaryAttributeData].
 */
fun interface BinaryAttributeDataNodeEncoder : AttributeDataEncoder<ConfigurationNode, BinaryAttributeData>

/**
 * Data conversion: [CompoundShadowTag] -> [BinaryAttributeData].
 */
fun interface BinaryAttributeDataNbtEncoder : AttributeDataEncoder<CompoundShadowTag, BinaryAttributeData>

/**
 * Data conversion: [BinaryAttributeData] -> [CompoundShadowTag].
 */
fun interface BinaryAttributeDataNbtDecoder : AttributeDataDecoder<CompoundShadowTag, BinaryAttributeData>

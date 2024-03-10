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

val PlainAttributeData.element: Element?
    @Suppress("UNCHECKED_CAST")
    get() = (this as? AttributeComponent.Element<Element>)?.element

/**
 * Represents the data of **fixed** attribute values.
 *
 * We especially extend [AttributeComponent.Op] as it's common among all subtypes.
 */
sealed interface PlainAttributeData : AttributeData, AttributeComponent.Op<Operation>, BinaryCoreData {
    data class S(
        override val operation: Operation,
        override val value: Double,
    ) : PlainAttributeData, AttributeDataS<Operation, Double>

    data class R(
        override val operation: Operation,
        override val lower: Double,
        override val upper: Double,
    ) : PlainAttributeData, AttributeDataR<Operation, Double>

    data class SE(
        override val operation: Operation,
        override val value: Double,
        override val element: Element,
    ) : PlainAttributeData, AttributeDataSE<Operation, Double, Element>

    data class RE(
        override val operation: Operation,
        override val lower: Double,
        override val upper: Double,
        override val element: Element,
    ) : PlainAttributeData, AttributeDataRE<Operation, Double, Element>
}

/**
 * Attribute Value Encoder: [ConfigurationNode] -> [PlainAttributeData].
 */
fun interface PlainAttributeDataNodeEncoder : AttributeDataEncoder<ConfigurationNode, PlainAttributeData>

// TODO see below
// 把 NBT 转换成 value 最好需要知道 NBT 的结构是什么，
// 从而最终推断出需要创建什么类型的 value。
// 虽然可以直接用 Compound#contains 来推断结构，
// 但这样不够效率，而且判断逻辑会随着格式种类的增多而增多。
/**
 * Attribute Value Encoder: [CompoundShadowTag] -> [PlainAttributeData].
 */
fun interface PlainAttributeDataNbtEncoder : AttributeDataEncoder<CompoundShadowTag, PlainAttributeData>

// TODO see below
// 把 value 转成 NBT 需要知道 NBT 具体的数值类型是什么，
// 例如有些是 Short，有些是 Int，有些是 Double，
// 这些数值类型需要根据该 value 所绑定的属性来得知。
/**
 * Attribute Value Encoder: [PlainAttributeData] -> [CompoundShadowTag].
 */
fun interface PlainAttributeDataNbtDecoder : AttributeDataDecoder<CompoundShadowTag, PlainAttributeData>

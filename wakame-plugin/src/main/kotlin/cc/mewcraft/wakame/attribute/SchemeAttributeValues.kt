package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.SchemeCoreValue
import cc.mewcraft.wakame.util.NumericValue
import cc.mewcraft.wakame.util.typedRequire
import org.spongepowered.configurate.ConfigurationNode

/**
 * 代表一个属性在模板中的数值。
 *
 * 模板中的数值可能为固定值，也可能为随机值，因此所有实现的数值都是 [NumericValue]
 */
sealed interface SchemeAttributeValue : SchemeCoreValue {
    val operation: AttributeModifier.Operation
}

////// 模板数据都支持随机数值

/**
 * S = Single.
 */
data class SchemeAttributeValueS(
    val value: NumericValue, override val operation: AttributeModifier.Operation,
) : SchemeAttributeValue {
    companion object : AttributeConfigSerializer<SchemeAttributeValueS> {
        override fun deserialize(node: ConfigurationNode): SchemeAttributeValueS {
            val value = deserializeSingle(node)
            val operation = deserializeOperation(node)
            return SchemeAttributeValueS(value, operation)
        }
    }
}

/**
 * LU = Lower & Upper.
 */
data class SchemeAttributeValueLU(
    val lower: NumericValue, val upper: NumericValue, override val operation: AttributeModifier.Operation,
) : SchemeAttributeValue {
    companion object : AttributeConfigSerializer<SchemeAttributeValueLU> {
        override fun deserialize(node: ConfigurationNode): SchemeAttributeValueLU {
            val lower = deserializeLower(node)
            val upper = deserializeUpper(node)
            val operation = deserializeOperation(node)
            return SchemeAttributeValueLU(lower, upper, operation)
        }
    }
}

/**
 * SE = Single & Element.
 */
data class SchemeAttributeValueSE(
    val value: NumericValue, val element: Element, override val operation: AttributeModifier.Operation,
) : SchemeAttributeValue {
    companion object : AttributeConfigSerializer<SchemeAttributeValueSE> {
        override fun deserialize(node: ConfigurationNode): SchemeAttributeValueSE {
            val value = deserializeSingle(node)
            val element = deserializeElement(node)
            val operation = deserializeOperation(node)
            return SchemeAttributeValueSE(value, element, operation)
        }
    }
}

/**
 * LUE = Lower & Upper & Element.
 */
data class SchemeAttributeValueLUE(
    val lower: NumericValue, val upper: NumericValue, val element: Element, override val operation: AttributeModifier.Operation,
) : SchemeAttributeValue {
    companion object : AttributeConfigSerializer<SchemeAttributeValueLUE> {
        override fun deserialize(node: ConfigurationNode): SchemeAttributeValueLUE {
            val lower = deserializeLower(node)
            val upper = deserializeUpper(node)
            val element = deserializeElement(node)
            val operation = deserializeOperation(node)
            return SchemeAttributeValueLUE(lower, upper, element, operation)
        }
    }
}

private fun deserializeSingle(node: ConfigurationNode): NumericValue =
    node.node("value").typedRequire<NumericValue>()

private fun deserializeLower(node: ConfigurationNode): NumericValue =
    node.node("lower").typedRequire<NumericValue>()

private fun deserializeUpper(node: ConfigurationNode): NumericValue =
    node.node("upper").typedRequire<NumericValue>()

private fun deserializeElement(node: ConfigurationNode): Element =
    node.node("element").typedRequire<Element>()

private fun deserializeOperation(node: ConfigurationNode): AttributeModifier.Operation =
    node.node("operation").string
        ?.let { AttributeModifier.Operation.byKey(it) }
        ?: AttributeModifier.Operation.ADDITION // ADDITION by default

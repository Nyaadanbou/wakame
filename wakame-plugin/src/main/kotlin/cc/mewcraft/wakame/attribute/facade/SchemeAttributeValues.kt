package cc.mewcraft.wakame.attribute.facade

import cc.mewcraft.wakame.attribute.base.AttributeModifier
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.SchemeCoreValue
import cc.mewcraft.wakame.util.NumericValue
import cc.mewcraft.wakame.util.requireKt
import org.spongepowered.configurate.ConfigurationNode

/**
 * 代表一个属性在模板中的数值。
 *
 * 模板中的数值可能为固定值，也可能为随机值，因此所有实现的数值都是 [NumericValue]
 */
sealed interface SchemeAttributeValue : SchemeCoreValue {
    val operation: AttributeModifier.Operation
}

/**
 * 代表一个属性模板的序列化器。
 */
sealed interface SchemeAttributeValueSerializer {
    fun serialize(node: ConfigurationNode, value: SchemeAttributeValue): Unit = throw UnsupportedOperationException()
    fun deserialize(node: ConfigurationNode): SchemeAttributeValue
}

////// 模板数据都支持随机数值

/**
 * S = Single.
 */
data class SchemeAttributeValueS(
    val value: NumericValue, override val operation: AttributeModifier.Operation,
) : SchemeAttributeValue

data object SchemeAttributeValueSerializerS : SchemeAttributeValueSerializer {
    override fun deserialize(node: ConfigurationNode): SchemeAttributeValueS {
        val value = deserializeSingle(node)
        val operation = deserializeOperation(node)
        return SchemeAttributeValueS(value, operation)
    }
}

/**
 * LU = Lower & Upper.
 */
data class SchemeAttributeValueLU(
    val lower: NumericValue, val upper: NumericValue, override val operation: AttributeModifier.Operation,
) : SchemeAttributeValue

data object SchemeAttributeValueSerializerLU : SchemeAttributeValueSerializer {
    override fun deserialize(node: ConfigurationNode): SchemeAttributeValueLU {
        val lower = deserializeLower(node)
        val upper = deserializeUpper(node)
        val operation = deserializeOperation(node)
        return SchemeAttributeValueLU(lower, upper, operation)
    }
}

/**
 * SE = Single & Element.
 */
data class SchemeAttributeValueSE(
    val value: NumericValue, val element: Element, override val operation: AttributeModifier.Operation,
) : SchemeAttributeValue

data object SchemeAttributeValueSerializerSE : SchemeAttributeValueSerializer {
    override fun deserialize(node: ConfigurationNode): SchemeAttributeValueSE {
        val value = deserializeSingle(node)
        val element = deserializeElement(node)
        val operation = deserializeOperation(node)
        return SchemeAttributeValueSE(value, element, operation)
    }
}

/**
 * LUE = Lower & Upper & Element.
 */
data class SchemeAttributeValueLUE(
    val lower: NumericValue, val upper: NumericValue, val element: Element, override val operation: AttributeModifier.Operation,
) : SchemeAttributeValue

data object SchemeAttributeValueSerializerLUE : SchemeAttributeValueSerializer {
    override fun deserialize(node: ConfigurationNode): SchemeAttributeValueLUE {
        val lower = deserializeLower(node)
        val upper = deserializeUpper(node)
        val element = deserializeElement(node)
        val operation = deserializeOperation(node)
        return SchemeAttributeValueLUE(lower, upper, element, operation)
    }
}

private fun deserializeSingle(node: ConfigurationNode): NumericValue =
    node.node("value").requireKt<NumericValue>()

private fun deserializeLower(node: ConfigurationNode): NumericValue =
    node.node("lower").requireKt<NumericValue>()

private fun deserializeUpper(node: ConfigurationNode): NumericValue =
    node.node("upper").requireKt<NumericValue>()

private fun deserializeElement(node: ConfigurationNode): Element =
    node.node("element").requireKt<Element>() // FIXME allow NullConfig

private fun deserializeOperation(node: ConfigurationNode): AttributeModifier.Operation =
    node.node("operation").string
        ?.let { AttributeModifier.Operation.byKey(it) }
        ?: AttributeModifier.Operation.ADD // ADDITION by default

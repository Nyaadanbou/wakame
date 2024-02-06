package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.SchemeCoreValue
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.NumericValue
import org.spongepowered.configurate.ConfigurationNode

/**
 * 代表一个属性在模板中的数值。
 *
 * 模板中的数值可能为固定值，也可能为随机值，因此所有实现的数值都是 [NumericValue]
 */
sealed interface SchemeAttributeValue : SchemeCoreValue

////// 模板数据都支持随机数值

data class SchemeAttributeValueV(
    val value: NumericValue, val operation: AttributeModifier.Operation,
) : SchemeAttributeValue {
    companion object : AttributeConfigSerializer<SchemeAttributeValueV> {
        override fun deserialize(node: ConfigurationNode): SchemeAttributeValueV {
            val value = getValue(node)
            val operation = getOperation(node)
            return SchemeAttributeValueV(value, operation)
        }
    }
}

data class SchemeAttributeValueLU(
    val lower: NumericValue, val upper: NumericValue, val operation: AttributeModifier.Operation,
) : SchemeAttributeValue {
    companion object : AttributeConfigSerializer<SchemeAttributeValueLU> {
        override fun deserialize(node: ConfigurationNode): SchemeAttributeValueLU {
            val lower = getLower(node)
            val upper = getUpper(node)
            val operation = getOperation(node)
            return SchemeAttributeValueLU(lower, upper, operation)
        }
    }
}

data class SchemeAttributeValueVE(
    val value: NumericValue, val element: Element, val operation: AttributeModifier.Operation,
) : SchemeAttributeValue {
    companion object : AttributeConfigSerializer<SchemeAttributeValueVE> {
        override fun deserialize(node: ConfigurationNode): SchemeAttributeValueVE {
            val value = getValue(node)
            val element = getElement(node)
            val operation = getOperation(node)
            return SchemeAttributeValueVE(value, element, operation)
        }
    }
}

data class SchemeAttributeValueLUE(
    val lower: NumericValue, val upper: NumericValue, val element: Element, val operation: AttributeModifier.Operation,
) : SchemeAttributeValue {
    companion object : AttributeConfigSerializer<SchemeAttributeValueLUE> {
        override fun deserialize(node: ConfigurationNode): SchemeAttributeValueLUE {
            val lower = getLower(node)
            val upper = getUpper(node)
            val element = getElement(node)
            val operation = getOperation(node)
            return SchemeAttributeValueLUE(lower, upper, element, operation)
        }
    }
}

private fun getValue(node: ConfigurationNode): NumericValue =
    requireNotNull(node.node("value")) { "`value` must be not null" }
        .let { NumericValue.create(it) }

private fun getUpper(node: ConfigurationNode) =
    requireNotNull(node.node("upper")) { "`upper` must be not null" }
        .let { NumericValue.create(it) }

private fun getLower(node: ConfigurationNode) =
    requireNotNull(node.node("lower")) { "`lower` must be not null" }
        .let { NumericValue.create(it) }

private fun getElement(node: ConfigurationNode) =
    requireNotNull(node.node("element").string) { "`element` must be not null" }
        .let { ElementRegistry.getOrThrow(it) }

private fun getOperation(node: ConfigurationNode) =
    node.node("operation").string?.let { AttributeModifier.Operation.byKey(it) }
        ?: AttributeModifier.Operation.ADDITION // ADDITION by default

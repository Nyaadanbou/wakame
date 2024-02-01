package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.SchemeTangValue
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.NumericValue
import org.spongepowered.configurate.ConfigurationNode

/**
 * 代表一个属性在模板中的数值。
 *
 * 模板中的数值可能为固定值，也可能为随机值，因此所有实现的数值都是 [NumericValue]
 */
sealed interface AttributeSchemaValue : SchemeTangValue

////// 模板数据都支持随机数值

data class AttributeSchemaValueV(
    val value: NumericValue, val operation: AttributeModifier.Operation,
) : AttributeSchemaValue {
    companion object : AttributeConfigSerializer<AttributeSchemaValueV> {
        override fun serialize(node: ConfigurationNode, value: AttributeSchemaValueV) {
            TODO("Not yet implemented")
        }

        override fun deserialize(node: ConfigurationNode): AttributeSchemaValueV {
            val value = getValue(node)
            val operation = getOperation(node)
            return AttributeSchemaValueV(value, operation)
        }
    }
}

data class AttributeSchemeValueLU(
    val lower: NumericValue, val upper: NumericValue, val operation: AttributeModifier.Operation,
) : AttributeSchemaValue {
    companion object : AttributeConfigSerializer<AttributeSchemeValueLU> {
        override fun serialize(node: ConfigurationNode, value: AttributeSchemeValueLU) {
            TODO("Not yet implemented")
        }

        override fun deserialize(node: ConfigurationNode): AttributeSchemeValueLU {
            val lower = getLower(node)
            val upper = getUpper(node)
            val operation = getOperation(node)
            return AttributeSchemeValueLU(lower, upper, operation)
        }
    }
}

data class AttributeSchemaValueVE(
    val value: NumericValue, val element: Element, val operation: AttributeModifier.Operation,
) : AttributeSchemaValue {
    companion object : AttributeConfigSerializer<AttributeSchemaValueVE> {
        override fun serialize(node: ConfigurationNode, value: AttributeSchemaValueVE) {
            TODO("Not yet implemented")
        }

        override fun deserialize(node: ConfigurationNode): AttributeSchemaValueVE {
            val value = getValue(node)
            val element = getElement(node)
            val operation = getOperation(node)
            return AttributeSchemaValueVE(value, element, operation)
        }
    }
}

data class AttributeSchemaValueLUE(
    val lower: NumericValue, val upper: NumericValue, val element: Element, val operation: AttributeModifier.Operation,
) : AttributeSchemaValue {
    companion object : AttributeConfigSerializer<AttributeSchemaValueLUE> {
        override fun serialize(node: ConfigurationNode, value: AttributeSchemaValueLUE) {
            TODO("Not yet implemented")
        }

        override fun deserialize(node: ConfigurationNode): AttributeSchemaValueLUE {
            val lower = getLower(node)
            val upper = getUpper(node)
            val element = getElement(node)
            val operation = getOperation(node)
            return AttributeSchemaValueLUE(lower, upper, element, operation)
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

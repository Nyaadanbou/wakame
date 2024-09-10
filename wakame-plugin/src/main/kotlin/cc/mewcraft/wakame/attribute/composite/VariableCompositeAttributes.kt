package cc.mewcraft.wakame.attribute.composite

import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.template.AttributeContextData
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.util.RandomizedValue
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import java.util.stream.Stream
import kotlin.math.max
import kotlin.math.min


/**
 * 该属性核心的元素种类. 如果该属性核心没有元素, 则返回 `null`.
 */
val VariableCompositeAttribute.element: Element?
    get() = (this as? CompositeAttributeComponent.Element)?.element

/**
 * 本函数用于构建 [VariableCompositeAttribute].
 */
fun VariableCompositeAttribute(
    type: String, node: ConfigurationNode
): VariableCompositeAttribute {
    return AttributeRegistry.FACADES[type].convertNode2Variable(node)
}

/**
 * 代表一个数值可以变化的 [CompositeAttribute].
 */
interface VariableCompositeAttribute : Examinable, CompositeAttribute {
    fun generate(context: AttributeGenerationContext): ConstantCompositeAttribute
}

/**
 * 代表一个生成 [CompositeAttribute] 的上下文.
 */
interface AttributeGenerationContext {
    /**
     * 当前已生成的等级.
     */
    var level: Int?

    /**
     * 当前已生成的属性.
     */
    val attributes: MutableCollection<AttributeContextData>
}


/* Implementations */


internal data class VariableCompositeAttributeR(
    override val id: String,
    override val operation: Operation,
    override val lower: RandomizedValue,
    override val upper: RandomizedValue,
) : VariableCompositeAttribute, CompositeAttributeR<RandomizedValue> {
    override fun generate(context: AttributeGenerationContext): ConstantCompositeAttributeR {
        populateContextWithDefault(context)
        val factor = context.level ?: 0
        val lower = lower.calculate(factor)
        val upper = upper.calculate(factor)
        return ConstantCompositeAttributeR(id, operation, min(lower, upper), max(lower, upper))
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("type", id),
        ExaminableProperty.of("operation", operation),
        ExaminableProperty.of("lower", lower),
        ExaminableProperty.of("upper", upper)
    )

    override fun toString(): String = toSimpleString()
}

internal data class VariableCompositeAttributeRE(
    override val id: String,
    override val operation: Operation,
    override val lower: RandomizedValue,
    override val upper: RandomizedValue,
    override val element: Element,
) : VariableCompositeAttribute, CompositeAttributeRE<RandomizedValue> {
    override fun generate(context: AttributeGenerationContext): ConstantCompositeAttribute {
        populateContextWithDefault(context)
        val factor = context.level ?: 0
        val lower = lower.calculate(factor)
        val upper = upper.calculate(factor)
        return ConstantCompositeAttributeRE(id, operation, min(lower, upper), max(lower, upper), element)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("type", id),
        ExaminableProperty.of("operation", operation),
        ExaminableProperty.of("lower", lower),
        ExaminableProperty.of("upper", upper),
        ExaminableProperty.of("element", element),
    )

    override fun toString(): String = toSimpleString()
}

internal data class VariableCompositeAttributeS(
    override val id: String,
    override val operation: Operation,
    override val value: RandomizedValue,
) : VariableCompositeAttribute, CompositeAttributeS<RandomizedValue> {
    override fun generate(context: AttributeGenerationContext): ConstantCompositeAttribute {
        populateContextWithDefault(context)
        val factor = context.level ?: 0
        val value = value.calculate(factor)
        return ConstantCompositeAttributeS(id, operation, value)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("type", id),
        ExaminableProperty.of("operation", operation),
        ExaminableProperty.of("value", value),
    )

    override fun toString(): String = toSimpleString()
}

internal data class VariableCompositeAttributeSE(
    override val id: String,
    override val operation: Operation,
    override val value: RandomizedValue,
    override val element: Element,
) : VariableCompositeAttribute, CompositeAttributeSE<RandomizedValue> {
    override fun generate(context: AttributeGenerationContext): ConstantCompositeAttribute {
        populateContextWithDefault(context)
        val factor = context.level ?: 0
        val value = value.calculate(factor)
        return ConstantCompositeAttributeSE(id, operation, value, element)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("type", id),
        ExaminableProperty.of("operation", operation),
        ExaminableProperty.of("value", value),
        ExaminableProperty.of("element", element),
    )

    override fun toString(): String = toSimpleString()
}

private fun VariableCompositeAttribute.populateContextWithDefault(context: AttributeGenerationContext) {
    context.attributes += AttributeContextData(id, operation, element)
}

package cc.mewcraft.wakame.attribute.composite

import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.template.AttributeContextData
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.util.RandomizedValue
import org.spongepowered.configurate.ConfigurationNode
import java.util.Objects
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
    type: String, node: ConfigurationNode,
): VariableCompositeAttribute = AttributeRegistry.FACADES[type].convertNode2Variable(node)

/**
 * 代表一个数值可以变化的 [CompositeAttribute].
 */
interface VariableCompositeAttribute : CompositeAttribute {
    fun generate(context: AttributeGenerationContext): Result

    /**
     * 封装了 [VariableCompositeAttribute] 的生成结果.
     *
     * @property value 生成的 [ConstantCompositeAttribute]
     * @property score 生成所使用的 Z-score
     */
    data class Result(
        val value: ConstantCompositeAttribute,
        val score: Array<Double>,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other)
                return true
            if (javaClass != other?.javaClass)
                return false
            other as Result
            if (value != other.value)
                return false
            if (!score.contentEquals(other.score))
                return false
            return true
        }

        override fun hashCode(): Int {
            return Objects.hash(value, score.contentHashCode())
        }
    }
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
    override fun generate(context: AttributeGenerationContext): VariableCompositeAttribute.Result {
        populateContextWithDefault(context)
        val factor = context.level ?: 0
        val (lower, score1) = lower.calculate(factor)
        val (upper, score2) = upper.calculate(factor)
        val attribute = ConstantCompositeAttributeR(id, operation, min(lower, upper), max(lower, upper))
        return VariableCompositeAttribute.Result(attribute, arrayOf(score1, score2))
    }
}

internal data class VariableCompositeAttributeRE(
    override val id: String,
    override val operation: Operation,
    override val lower: RandomizedValue,
    override val upper: RandomizedValue,
    override val element: Element,
) : VariableCompositeAttribute, CompositeAttributeRE<RandomizedValue> {
    override fun generate(context: AttributeGenerationContext): VariableCompositeAttribute.Result {
        populateContextWithDefault(context)
        val factor = context.level ?: 0
        val (lower, score1) = lower.calculate(factor)
        val (upper, score2) = upper.calculate(factor)
        val attribute = ConstantCompositeAttributeRE(id, operation, min(lower, upper), max(lower, upper), element)
        return VariableCompositeAttribute.Result(attribute, arrayOf(score1, score2))
    }
}

internal data class VariableCompositeAttributeS(
    override val id: String,
    override val operation: Operation,
    override val value: RandomizedValue,
) : VariableCompositeAttribute, CompositeAttributeS<RandomizedValue> {
    override fun generate(context: AttributeGenerationContext): VariableCompositeAttribute.Result {
        populateContextWithDefault(context)
        val factor = context.level ?: 0
        val (value, score) = value.calculate(factor)
        val attribute = ConstantCompositeAttributeS(id, operation, value)
        return VariableCompositeAttribute.Result(attribute, arrayOf(score))
    }
}

internal data class VariableCompositeAttributeSE(
    override val id: String,
    override val operation: Operation,
    override val value: RandomizedValue,
    override val element: Element,
) : VariableCompositeAttribute, CompositeAttributeSE<RandomizedValue> {
    override fun generate(context: AttributeGenerationContext): VariableCompositeAttribute.Result {
        populateContextWithDefault(context)
        val factor = context.level ?: 0
        val (value, score) = value.calculate(factor)
        val attribute = ConstantCompositeAttributeSE(id, operation, value, element)
        return VariableCompositeAttribute.Result(attribute, arrayOf(score))
    }
}

private fun VariableCompositeAttribute.populateContextWithDefault(context: AttributeGenerationContext) {
    context.attributes += AttributeContextData(id, operation, element)
}

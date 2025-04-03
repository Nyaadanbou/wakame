package cc.mewcraft.wakame.attribute.bundle

import cc.mewcraft.wakame.element.ElementType
import cc.mewcraft.wakame.entity.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.item.template.AttributeContextData
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.util.RandomizedValue
import org.spongepowered.configurate.ConfigurationNode
import kotlin.math.max
import kotlin.math.min


/**
 * 该属性核心的元素种类. 如果该属性核心没有元素, 则返回 `null`.
 */
val VariableAttributeBundle.element: RegistryEntry<ElementType>?
    get() = (this as? AttributeBundleTrait.Element)?.element

/**
 * 本函数用于构建 [VariableAttributeBundle].
 */
fun VariableAttributeBundle(
    type: String, node: ConfigurationNode,
): VariableAttributeBundle = KoishRegistries.ATTRIBUTE_BUNDLE_FACADE.getOrThrow(type).convertNodeToVariable(node)

/**
 * 代表一个数值可以变化的 [AttributeBundle].
 */
interface VariableAttributeBundle : AttributeBundle {
    fun generate(context: AttributeGenerationContext): ConstantAttributeBundle
}

/**
 * 代表一个生成 [AttributeBundle] 的上下文.
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


internal data class VariableAttributeBundleR(
    override val id: String,
    override val operation: Operation,
    override val lower: RandomizedValue,
    override val upper: RandomizedValue,
) : VariableAttributeBundle, AttributeBundleR<RandomizedValue> {
    override fun generate(context: AttributeGenerationContext): ConstantAttributeBundle {
        populateContextWithDefault(context)
        val factor = context.level ?: 0
        val (lower, score1) = lower.calculate(factor)
        val (upper, score2) = upper.calculate(factor)
        return ConstantAttributeBundleR(
            id,
            operation,
            min(lower, upper),
            max(lower, upper),
            ConstantAttributeBundle.Quality.fromZScore(score2)
        )
    }
}

internal data class VariableAttributeBundleRE(
    override val id: String,
    override val operation: Operation,
    override val lower: RandomizedValue,
    override val upper: RandomizedValue,
    override val element: RegistryEntry<ElementType>,
) : VariableAttributeBundle, AttributeBundleRE<RandomizedValue> {
    override fun generate(context: AttributeGenerationContext): ConstantAttributeBundle {
        populateContextWithDefault(context)
        val factor = context.level ?: 0
        val (lower, score1) = lower.calculate(factor)
        val (upper, score2) = upper.calculate(factor)
        return ConstantAttributeBundleRE(
            id,
            operation,
            min(lower, upper),
            max(lower, upper),
            element,
            ConstantAttributeBundle.Quality.fromZScore(score2)
        )
    }
}

internal data class VariableAttributeBundleS(
    override val id: String,
    override val operation: Operation,
    override val value: RandomizedValue,
) : VariableAttributeBundle, AttributeBundleS<RandomizedValue> {
    override fun generate(context: AttributeGenerationContext): ConstantAttributeBundle {
        populateContextWithDefault(context)
        val factor = context.level ?: 0
        val (value, score) = value.calculate(factor)
        return ConstantAttributeBundleS(
            id,
            operation,
            value,
            ConstantAttributeBundle.Quality.fromZScore(score)
        )
    }
}

internal data class VariableAttributeBundleSE(
    override val id: String,
    override val operation: Operation,
    override val value: RandomizedValue,
    override val element: RegistryEntry<ElementType>,
) : VariableAttributeBundle, AttributeBundleSE<RandomizedValue> {
    override fun generate(context: AttributeGenerationContext): ConstantAttributeBundle {
        populateContextWithDefault(context)
        val factor = context.level ?: 0
        val (value, score) = value.calculate(factor)
        return ConstantAttributeBundleSE(
            id,
            operation,
            value,
            element,
            ConstantAttributeBundle.Quality.fromZScore(score)
        )
    }
}

private fun VariableAttributeBundle.populateContextWithDefault(context: AttributeGenerationContext) {
    context.attributes += AttributeContextData(id, operation, element)
}

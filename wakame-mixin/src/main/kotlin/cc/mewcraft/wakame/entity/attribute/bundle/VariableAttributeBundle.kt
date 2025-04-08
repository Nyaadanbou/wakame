package cc.mewcraft.wakame.entity.attribute.bundle

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.entity.attribute.AttributeModifier
import cc.mewcraft.wakame.item2.config.datagen.LevelContext
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.util.RandomizedValue
import org.spongepowered.configurate.ConfigurationNode
import kotlin.math.max
import kotlin.math.min


/**
 * 该属性核心的元素种类. 如果该属性核心没有元素, 则返回 `null`.
 */
val VariableAttributeBundle.element: RegistryEntry<Element>?
    get() = (this as? AttributeBundleTrait.Element)?.element

/**
 * 本函数用于构建 [VariableAttributeBundle].
 */
fun VariableAttributeBundle(
    type: String, node: ConfigurationNode,
): VariableAttributeBundle = BuiltInRegistries.ATTRIBUTE_FACADE.getOrThrow(type).convertNodeToVariable(node)

/**
 * 代表一个数值可以变化的 [AttributeBundle].
 */
interface VariableAttributeBundle : AttributeBundle {
    fun generate(context: AttributeContext): ConstantAttributeBundle
}

/**
 * 代表一个生成 [AttributeBundle] 的上下文.
 */
interface AttributeContext : LevelContext {
    val attributes: MutableCollection<AttributeContextData>
}

/**
 * 代表一个生成 [AttributeBundle] 的上下文数据.
 */
data class AttributeContextData(
    val id: String,
    val operation: AttributeModifier.Operation?,
    val element: RegistryEntry<Element>?,
)


/* Implementations */


data class VariableAttributeBundleR(
    override val id: String,
    override val operation: AttributeModifier.Operation,
    override val lower: RandomizedValue,
    override val upper: RandomizedValue,
) : VariableAttributeBundle, AttributeBundleR<RandomizedValue> {
    override fun generate(context: AttributeContext): ConstantAttributeBundle {
        populateContextWithDefault(context)
        val factor = context.level
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

data class VariableAttributeBundleRE(
    override val id: String,
    override val operation: AttributeModifier.Operation,
    override val lower: RandomizedValue,
    override val upper: RandomizedValue,
    override val element: RegistryEntry<Element>,
) : VariableAttributeBundle, AttributeBundleRE<RandomizedValue> {
    override fun generate(context: AttributeContext): ConstantAttributeBundle {
        populateContextWithDefault(context)
        val factor = context.level
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

data class VariableAttributeBundleS(
    override val id: String,
    override val operation: AttributeModifier.Operation,
    override val value: RandomizedValue,
) : VariableAttributeBundle, AttributeBundleS<RandomizedValue> {
    override fun generate(context: AttributeContext): ConstantAttributeBundle {
        populateContextWithDefault(context)
        val factor = context.level
        val (value, score) = value.calculate(factor)
        return ConstantAttributeBundleS(
            id,
            operation,
            value,
            ConstantAttributeBundle.Quality.fromZScore(score)
        )
    }
}

data class VariableAttributeBundleSE(
    override val id: String,
    override val operation: AttributeModifier.Operation,
    override val value: RandomizedValue,
    override val element: RegistryEntry<Element>,
) : VariableAttributeBundle, AttributeBundleSE<RandomizedValue> {
    override fun generate(context: AttributeContext): ConstantAttributeBundle {
        populateContextWithDefault(context)
        val factor = context.level
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

private fun VariableAttributeBundle.populateContextWithDefault(context: AttributeContext) {
    context.attributes += AttributeContextData(id, operation, element)
}

package cc.mewcraft.wakame.item.components.cells.template.cores.attribute

import cc.mewcraft.nbt.TagType
import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.attribute.facade.AttributeComponent
import cc.mewcraft.wakame.attribute.facade.AttributeData
import cc.mewcraft.wakame.attribute.facade.AttributeDataR
import cc.mewcraft.wakame.attribute.facade.AttributeDataRE
import cc.mewcraft.wakame.attribute.facade.AttributeDataS
import cc.mewcraft.wakame.attribute.facade.AttributeDataSE
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttribute
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttributeR
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttributeRE
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttributeS
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttributeSE
import cc.mewcraft.wakame.item.components.cells.template.TemplateCore
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.templates.filter.AttributeContextHolder
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.util.RandomizedValue
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.ExaminableProperty
import org.spongepowered.configurate.ConfigurationNode
import java.util.stream.Stream
import kotlin.math.max
import kotlin.math.min

/**
 * 一个属性核心的模板.
 */
interface TemplateCoreAttribute : TemplateCore, AttributeData, AttributeComponent.Op {
    override fun generate(context: GenerationContext): CoreAttribute
}

/**
 * 构建一个 [TemplateCoreAttribute].
 */
fun TemplateCoreAttribute(node: ConfigurationNode): TemplateCoreAttribute {
    val key = node.node("key").krequire<Key>()
    val template = AttributeRegistry.FACADES[key].schemaCoreCreatorByConfig(node) // TODO 这些涉及到数据转换的全部以 codec 开头
    return template
}

/* Implementations */

internal val TemplateCoreAttribute.element: Element?
    get() = (this as? AttributeComponent.Element)?.element

internal data class TemplateCoreAttributeR(
    private val tagType: TagType,
    override val key: Key,
    override val operation: Operation,
    override val lower: RandomizedValue,
    override val upper: RandomizedValue,
) : TemplateCoreAttribute, AttributeDataR<RandomizedValue> {
    override fun generate(context: GenerationContext): CoreAttribute {
        populateContextWithDefault(context)
        val factor = context.levelOrThrow
        val lower = lower.calculate(factor)
        val upper = upper.calculate(factor)
        return CoreAttributeR(tagType, key, operation, min(lower, upper), max(lower, upper))
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("tagType", tagType),
        ExaminableProperty.of("operation", operation),
        ExaminableProperty.of("lower", lower),
        ExaminableProperty.of("upper", upper)
    )

    override fun toString(): String = toSimpleString()
}

internal data class TemplateCoreAttributeRE(
    private val tagType: TagType,
    override val key: Key,
    override val operation: Operation,
    override val lower: RandomizedValue,
    override val upper: RandomizedValue,
    override val element: Element,
) : TemplateCoreAttribute, AttributeDataRE<RandomizedValue> {
    override fun generate(context: GenerationContext): CoreAttribute {
        populateContextWithDefault(context)
        val factor = context.levelOrThrow
        val lower = lower.calculate(factor)
        val upper = upper.calculate(factor)
        return CoreAttributeRE(tagType, key, operation, min(lower, upper), max(lower, upper), element)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("tagType", tagType),
        ExaminableProperty.of("operation", operation),
        ExaminableProperty.of("lower", lower),
        ExaminableProperty.of("upper", upper),
        ExaminableProperty.of("element", element),
    )

    override fun toString(): String = toSimpleString()
}

internal data class TemplateCoreAttributeS(
    private val tagType: TagType,
    override val key: Key,
    override val operation: Operation,
    override val value: RandomizedValue,
) : TemplateCoreAttribute, AttributeDataS<RandomizedValue> {
    override fun generate(context: GenerationContext): CoreAttribute {
        populateContextWithDefault(context)
        val factor = context.levelOrThrow
        val value = value.calculate(factor)
        return CoreAttributeS(tagType, key, operation, value)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("tagType", tagType),
        ExaminableProperty.of("operation", operation),
        ExaminableProperty.of("value", value),
    )

    override fun toString(): String = toSimpleString()
}

internal data class TemplateCoreAttributeSE(
    private val tagType: TagType,
    override val key: Key,
    override val operation: Operation,
    override val value: RandomizedValue,
    override val element: Element,
) : TemplateCoreAttribute, AttributeDataSE<RandomizedValue> {
    override fun generate(context: GenerationContext): CoreAttribute {
        populateContextWithDefault(context)
        val factor = context.levelOrThrow
        val value = value.calculate(factor)
        return CoreAttributeSE(tagType, key, operation, value, element)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key),
        ExaminableProperty.of("tagType", tagType),
        ExaminableProperty.of("operation", operation),
        ExaminableProperty.of("value", value),
        ExaminableProperty.of("element", element),
    )

    override fun toString(): String = toSimpleString()
}

private fun TemplateCoreAttribute.populateContextWithDefault(context: GenerationContext) {
    context.attributes += AttributeContextHolder(key, operation, element)
}

private fun <T : Number> ensureNonNullLevel(level: T?): T {
    return requireNotNull(level) { "Failed to generate ${TemplateCoreAttribute::class.simpleName} due to level not presenting in the generation context" }
}
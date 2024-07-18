package cc.mewcraft.wakame.item.templates.filter

import cc.mewcraft.wakame.attribute.AttributeModifier.Operation
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.templates.filter.FilterSerializer.NAMESPACE_FILTER
import cc.mewcraft.wakame.random3.Filter
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.Objects
import java.util.stream.Stream

/**
 * Checks attribute population.
 */
data class FilterAttribute(
    override val invert: Boolean,
    private val key: Key,
    private val operation: Operation?,
    private val element: Element?,
) : Filter<GenerationContext>, Examinable {
    companion object {
        val TYPE = Key.key(NAMESPACE_FILTER, "attribute")
    }

    override val type: Key = TYPE

    /**
     * Returns `true` if the [context] already has the attribute with
     * [key][key] populated.
     */
    override fun testOriginal(context: GenerationContext): Boolean {
        return AttributeContextHolder(key, operation, element) in context.attributes
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("invert", invert),
            ExaminableProperty.of("key", key),
            ExaminableProperty.of("operation", operation),
            ExaminableProperty.of("element", element),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

data class AttributeContextHolder(
    val key: Key,
    val operation: Operation?,
    val element: Element?,
) : Examinable {
    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key.asString()),
        ExaminableProperty.of("operation", operation?.key),
        ExaminableProperty.of("element", element?.uniqueId),
    )

    override fun toString(): String = toSimpleString()
    override fun hashCode(): Int = Objects.hash(key, operation, element)
    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other is AttributeContextHolder)
            return key == other.key && operation == other.operation && element == other.element
        return false
    }
}

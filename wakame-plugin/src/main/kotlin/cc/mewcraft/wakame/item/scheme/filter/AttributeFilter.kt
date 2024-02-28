package cc.mewcraft.wakame.item.scheme.filter

import cc.mewcraft.wakame.attribute.base.AttributeModifier
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import net.kyori.adventure.key.Key
import java.util.Objects

class AttributeFilter(
    override val invert: Boolean,
    private val key: Key,
    private val operation: AttributeModifier.Operation,
    private val element: Element?,
) : Filter {
    override fun test0(context: SchemeGenerationContext): Boolean {
        return AttributeContextHolder(key, operation, element) in context.attributes
    }
}

data class AttributeContextHolder(
    val key: Key,
    val operation: AttributeModifier.Operation,
    val element: Element?,
) {
    override fun hashCode(): Int {
        return Objects.hash(key, operation, element)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is AttributeContextHolder)
            return key == other.key && operation == other.operation && element == other.element
        return false
    }
}

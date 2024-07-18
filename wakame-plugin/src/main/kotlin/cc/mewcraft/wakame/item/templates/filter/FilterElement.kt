package cc.mewcraft.wakame.item.templates.filter

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.templates.filter.FilterSerializer.NAMESPACE_FILTER
import cc.mewcraft.wakame.random3.Filter
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * Checks [element] population.
 *
 * This filter can be used to ensure only **appropriate** elemental
 * attributes to be populated in the generation process. For example, you
 * can use the filter to only populate specific elemental attributes, which
 * avoids the situation where the item could have both "fire attack damage"
 * and "water attack damage rate" attributes simultaneously. In that case,
 * the "water attack damage rate" literally takes no effect, which doesn't
 * make sense to players.
 *
 * @property element the element to check with
 */
data class FilterElement(
    override val invert: Boolean,
    private val element: Element,
) : Filter<GenerationContext>, Examinable {
    companion object {
        val TYPE = Key.key(NAMESPACE_FILTER, "element")
    }

    override val type: Key = TYPE

    /**
     * Returns `true` if the [context] already has the [element] populated.
     */
    override fun testOriginal(context: GenerationContext): Boolean {
        return element in context.elements
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("invert", invert),
            ExaminableProperty.of("element", element),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}
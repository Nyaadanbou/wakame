package cc.mewcraft.wakame.item.schema.filter

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext

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
data class ElementFilter(
    override val invert: Boolean,
    private val element: Element,
) : Filter {

    /**
     * Returns `true` if the [context] already has the [element] populated.
     */
    override fun testRaw(context: SchemaGenerationContext): Boolean {
        return element in context.elements
    }
}
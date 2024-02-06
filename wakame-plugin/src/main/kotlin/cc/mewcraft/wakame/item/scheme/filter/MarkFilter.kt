package cc.mewcraft.wakame.item.scheme.filter

import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.random.Mark

/**
 * Checks [mark] population.
 *
 * @property mark the mark value in string to check with
 */
class MarkFilter(
    override val invert: Boolean,
    private val mark: String,
) : Filter {

    /**
     * Returns `true` if the [context] already has the [mark] populated.
     */
    override fun test0(context: SchemeGenerationContext): Boolean {
        return Mark.stringMarkOf(mark) in context.marks
    }
}
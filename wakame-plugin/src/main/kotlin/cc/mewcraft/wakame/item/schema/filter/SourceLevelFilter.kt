package cc.mewcraft.wakame.item.schema.filter

import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import com.google.common.collect.Range

/**
 * Checks source level population.
 *
 * @property invert
 * @property level
 */
data class SourceLevelFilter(
    override val invert: Boolean,
    private val level: Range<Int>,
) : Filter {

    /**
     * Returns `true` if the source level in the [context] is in the range of
     * [level].
     */
    override fun test0(context: SchemaGenerationContext): Boolean {
        return (context.trigger.level) in level
    }
}
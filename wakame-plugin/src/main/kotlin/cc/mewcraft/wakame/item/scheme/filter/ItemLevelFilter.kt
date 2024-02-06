package cc.mewcraft.wakame.item.scheme.filter

import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import com.google.common.collect.Range

class ItemLevelFilter(
    override val invert: Boolean,
    private val level: Range<Int>,
) : Filter {

    /**
     * Returns `true` if the item level in the [context] is in the range of
     * [level].
     */
    override fun test0(context: SchemeGenerationContext): Boolean {
        return context.itemLevel in level
    }
}
package cc.mewcraft.wakame.item.schema.filter

import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import com.google.common.collect.Range

data class ItemLevelFilter(
    override val invert: Boolean,
    private val level: Range<Int>,
) : Filter {

    /**
     * Returns `true` if the item level in the [context] is in the range of
     * [level].
     */
    override fun testRaw(context: SchemaGenerationContext): Boolean {
        return context.level in level
    }
}
package cc.mewcraft.wakame.item.schema.cell.curse

import cc.mewcraft.wakame.item.Curse
import cc.mewcraft.wakame.item.binary.cell.curse.BinaryCurse
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext

/**
 * Represents a [Curse] in schema form.
 */
interface SchemaCurse : Curse {
    /**
     * Generates a binary curse from this schema.
     *
     * @param context the context
     * @return a new instance
     */
    fun reify(context: SchemaGenerationContext): BinaryCurse
}

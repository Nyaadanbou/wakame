package cc.mewcraft.wakame.item.schema.cell.curse

import cc.mewcraft.wakame.item.Curse
import cc.mewcraft.wakame.item.binary.cell.curse.BinaryCurse
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext

/**
 * Represents a [Curse] in schema form.
 */
interface SchemaCurse : Curse {
    /**
     * Reifies the schema.
     *
     * **The implementation must populate the [context] with correct information!**
     *
     * @param context the generation context
     * @return a new instance of [BinaryCurse]
     */
    fun reify(context: SchemaGenerationContext): BinaryCurse
}

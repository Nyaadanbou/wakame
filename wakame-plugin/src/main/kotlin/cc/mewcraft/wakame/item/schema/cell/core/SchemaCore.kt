package cc.mewcraft.wakame.item.schema.cell.core

import cc.mewcraft.wakame.item.Core
import cc.mewcraft.wakame.item.binary.cell.core.BinaryCore
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext

/**
 * Represents a **schema core**.
 *
 * The name "schema" implies that this [core][Core] is a config
 * representation, i.e., what it looks like in the configuration.
 */
interface SchemaCore : Core {
    /**
     * Generates a binary core data from this schema.
     *
     * @param context the generation context
     * @return a new instance
     */
    fun generate(context: SchemaGenerationContext): BinaryCore
}

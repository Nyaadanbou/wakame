package cc.mewcraft.wakame.item.schema.cell.core

import cc.mewcraft.wakame.item.Core
import cc.mewcraft.wakame.item.CoreData
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext

/**
 * Represents a **schema core**.
 *
 * The name "schema" implies that this [core][Core] is a config
 * representation, i.e., what it looks like in the configuration.
 */
sealed interface SchemaCore : Core {
    /**
     * The schema data.
     */
    val data: CoreData.Schema

    /**
     * Generates a binary core data from `this`.
     *
     * @param context the context
     * @return a new instance
     */
    fun generate(context: SchemaGenerationContext): CoreData.Binary
}

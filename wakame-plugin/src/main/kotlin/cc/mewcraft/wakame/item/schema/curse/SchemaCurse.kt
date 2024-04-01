package cc.mewcraft.wakame.item.schema.curse

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.item.Curse
import cc.mewcraft.wakame.item.binary.curse.BinaryCurse
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext

/**
 * Represents a [curse][Curse] in schema form.
 */
sealed interface SchemaCurse : Curse {
    /**
     * Generates a binary course from `this`.
     *
     * @param context the context
     * @return a new instance
     */
    fun generate(context: SchemaGenerationContext): BinaryCurse
}

/**
 * Gets the empty condition.
 */
fun emptySchemaCurse(): SchemaCurse = @OptIn(InternalApi::class) EmptySchemaCurse

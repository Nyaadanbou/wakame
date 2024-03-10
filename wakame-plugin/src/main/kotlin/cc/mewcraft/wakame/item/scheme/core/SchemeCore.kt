package cc.mewcraft.wakame.item.scheme.core

import cc.mewcraft.wakame.item.BinaryCoreData
import cc.mewcraft.wakame.item.Core
import cc.mewcraft.wakame.item.SchemaCoreData
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext

/**
 * Represents a **schema core**.
 *
 * The name "schema" implies that this [core][Core] is a config
 * representation, i.e., what it looks like in the configuration.
 */
sealed interface SchemeCore : Core {
    /**
     * The scheme value.
     */
    val value: SchemaCoreData

    /**
     * Generates a binary core value from `this`.
     *
     * @param context the context
     * @return a new instance
     */
    fun generate(context: SchemeGenerationContext): BinaryCoreData
}

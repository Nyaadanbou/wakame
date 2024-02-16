package cc.mewcraft.wakame.item.scheme.core

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.item.BinaryCoreValue
import cc.mewcraft.wakame.item.Core
import cc.mewcraft.wakame.item.SchemeCoreValue
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext

/**
 * Represents a **scheme** [Core].
 *
 * The name "scheme" implies that this [core][Core] is a config
 * representation; that is, what it looks like in the configuration.
 */
sealed interface SchemeCore : Core {
    /**
     * The scheme value.
     */
    val value: SchemeCoreValue

    /**
     * Generates a binary core value from `this`.
     *
     * @param context the context
     * @return a new instance
     */
    fun generate(context: SchemeGenerationContext): BinaryCoreValue
}

/**
 * Gets the empty core.
 */
fun emptySchemeCore(): SchemeCore = @OptIn(InternalApi::class) EmptySchemeCore


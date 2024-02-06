package cc.mewcraft.wakame.item.scheme.core

import cc.mewcraft.wakame.item.BinaryCoreValue
import cc.mewcraft.wakame.item.Core
import cc.mewcraft.wakame.item.SchemeCoreValue

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
     * @param scalingFactor the scaling factor, such as player level
     * @return a new [binary core value][BinaryCoreValue]
     */
    fun generate(scalingFactor: Int): BinaryCoreValue
}

/**
 * Gets the empty core.
 */
fun emptySchemeCore(): SchemeCore = EmptySchemeCore


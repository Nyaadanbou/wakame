package cc.mewcraft.wakame.item.scheme.curse

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.item.Curse
import cc.mewcraft.wakame.item.binary.curse.BinaryCurse
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext

/**
 * Represents a [curse][Curse] in scheme form.
 */
sealed interface SchemeCurse : Curse {
    /**
     * Generates a binary course from `this`.
     *
     * @param context the context
     * @return a new instance
     */
    fun generate(context: SchemeGenerationContext): BinaryCurse
}

/**
 * Gets the empty condition.
 */
fun emptySchemeCurse(): SchemeCurse = @OptIn(InternalApi::class) EmptySchemeCurse

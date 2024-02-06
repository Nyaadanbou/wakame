package cc.mewcraft.wakame.item.scheme.curse

import cc.mewcraft.wakame.item.Curse
import cc.mewcraft.wakame.item.binary.curse.BinaryCurse

/**
 * Represents a [curse][Curse] in scheme form.
 */
sealed interface SchemeCurse : Curse {
    /**
     * Generates a binary course from `this`.
     *
     * @param scalingFactor the scaling factor, such as player level
     * @return a new [binary curse][BinaryCurse]
     */
    fun generate(scalingFactor: Int): BinaryCurse
}

/**
 * Gets the empty condition.
 */
fun emptySchemeCurse(): SchemeCurse = EmptySchemeCurse

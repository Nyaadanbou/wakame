package cc.mewcraft.wakame.item.scheme.filter

import cc.mewcraft.wakame.item.Core
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import net.kyori.adventure.key.Key

/**
 * Checks [core][Core] population.
 *
 * This could be, for example, used to check whether a [core][Core]
 * with key `attribute:critical_strike_chance` has been populated.
 *
 * @property key the key of the [core][Core] to check with
 */
data class CoreFilter(
    override val invert: Boolean,
    private val key: Key,
) : Filter {

    /**
     * Returns `true` if the [context] already has the [core][Core] with
     * [key][key] populated.
     */
    override fun test0(context: SchemeGenerationContext): Boolean {
        return key in context.coreKeys
    }
}
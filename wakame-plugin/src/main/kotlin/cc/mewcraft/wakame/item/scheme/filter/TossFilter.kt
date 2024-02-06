package cc.mewcraft.wakame.item.scheme.filter

import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import kotlin.random.Random

/**
 * Checks [probability].
 *
 * @property probability the probability of success for this toss
 */
class TossFilter(
    override val invert: Boolean,
    private val probability: Float,
) : Filter {

    /**
     * Returns `true` if the toss is success.
     */
    override fun test0(context: SchemeGenerationContext): Boolean {
        return Random.nextFloat() < probability
    }
}
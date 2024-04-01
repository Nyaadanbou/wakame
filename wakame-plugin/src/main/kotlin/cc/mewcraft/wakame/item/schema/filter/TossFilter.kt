package cc.mewcraft.wakame.item.schema.filter

import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import kotlin.random.Random

/**
 * Checks [probability].
 *
 * @property probability the probability of success for this toss
 */
data class TossFilter(
    override val invert: Boolean,
    private val probability: Float,
) : Filter {

    /**
     * Returns `true` if the toss is success.
     */
    override fun test0(context: SchemaGenerationContext): Boolean {
        return Random.nextFloat() < probability
    }
}
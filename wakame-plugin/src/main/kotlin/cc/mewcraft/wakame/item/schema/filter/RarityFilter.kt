package cc.mewcraft.wakame.item.schema.filter

import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.rarity.Rarity

/**
 * Checks [rarity] population.
 *
 * @property rarity the [Rarity] to check with
 */
data class RarityFilter(
    override val invert: Boolean,
    private val rarity: Rarity,
) : Filter {

    /**
     * Returns `true` if the [context] already has the [rarity] populated.
     */
    override fun testRaw(context: SchemaGenerationContext): Boolean {
        return rarity in context.rarities
    }
}
package cc.mewcraft.wakame.item.templates.filter

import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.random3.Filter
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * Checks [rarity] population.
 *
 * @property rarity the [Rarity] to check with
 */
data class FilterRarity(
    override val invert: Boolean,
    private val rarity: Rarity,
) : Filter<GenerationContext>, Examinable {

    /**
     * Returns `true` if the [context] already has the [rarity] populated.
     */
    override fun testOriginal(context: GenerationContext): Boolean {
        return rarity == context.rarity
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("invert", invert),
            ExaminableProperty.of("rarity", rarity),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}
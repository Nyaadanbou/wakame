package cc.mewcraft.wakame.item.templates.filter

import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.random3.Filter
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream
import kotlin.random.Random

/**
 * Checks [probability].
 *
 * @property probability the probability of success for this toss
 */
data class FilterToss(
    override val invert: Boolean,
    private val probability: Float,
) : Filter<GenerationContext>, Examinable {

    /**
     * Returns `true` if the toss is success.
     */
    override fun testOriginal(context: GenerationContext): Boolean {
        return Random.nextFloat() < probability
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("invert", invert),
            ExaminableProperty.of("probability", probability),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}
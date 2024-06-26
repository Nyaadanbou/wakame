package cc.mewcraft.wakame.item.filter

import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.random2.Filter
import cc.mewcraft.wakame.random2.Mark
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * Checks [mark] population.
 *
 * @property mark the mark value in string to check with
 */
data class FilterMark(
    override val invert: Boolean,
    private val mark: String,
) : Filter<GenerationContext>, Examinable {

    /**
     * Returns `true` if the [context] already has the [mark] populated.
     */
    override fun testOriginal(context: GenerationContext): Boolean {
        return Mark.stringMarkOf(mark) in context.marks
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("invert", invert),
            ExaminableProperty.of("mark", mark),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}
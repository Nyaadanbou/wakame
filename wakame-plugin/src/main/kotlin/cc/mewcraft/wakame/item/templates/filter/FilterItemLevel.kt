package cc.mewcraft.wakame.item.templates.filter

import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.random2.Filter
import cc.mewcraft.wakame.util.toSimpleString
import cc.mewcraft.wakame.util.toStableInt
import com.google.common.collect.Range
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

data class FilterItemLevel(
    override val invert: Boolean,
    private val level: Range<Int>,
) : Filter<GenerationContext>, Examinable {

    /**
     * Returns `true` if the item level in the [context] is in the range of
     * [level].
     */
    override fun testOriginal(context: GenerationContext): Boolean {
        val level = context.level
        if (level != null) {
            return level.toStableInt() in this.level
        }
        return false
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("invert", invert),
            ExaminableProperty.of("level", level),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}
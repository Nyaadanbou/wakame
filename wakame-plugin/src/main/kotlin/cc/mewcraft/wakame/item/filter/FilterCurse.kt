package cc.mewcraft.wakame.item.filter

import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.random2.Filter
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * Checks curse population.
 */
class FilterCurse(
    override val invert: Boolean,
    private val key: Key,
) : Filter<GenerationContext>, Examinable {

    /**
     * Returns `true` if the [context] already has the curse with
     * [key][key] populated.
     */
    override fun testOriginal(context: GenerationContext): Boolean {
        return CurseContextHolder(key) in context.curses
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("invert", invert),
            ExaminableProperty.of("key", key),
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

data class CurseContextHolder(
    val key: Key,
) : Examinable {
    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("key", key.asString()),
    )

    override fun toString(): String = toSimpleString()
    override fun hashCode(): Int = key.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other is CurseContextHolder)
            return key == other.key
        return false
    }
}
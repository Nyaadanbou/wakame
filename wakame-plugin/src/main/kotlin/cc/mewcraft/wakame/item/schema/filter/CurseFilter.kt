package cc.mewcraft.wakame.item.schema.filter

import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * Checks curse population.
 */
class CurseFilter(
    override val invert: Boolean,
    private val key: Key,
) : Filter {

    /**
     * Returns `true` if the [context] already has the curse with
     * [key][key] populated.
     */
    override fun test0(context: SchemaGenerationContext): Boolean {
        return CurseContextHolder(key) in context.curses
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
        if (this === other) return true
        if (other is AttributeContextHolder)
            return key == other.key
        return false
    }
}
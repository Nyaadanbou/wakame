package cc.mewcraft.wakame.item.scheme.filter

import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * Checks ability population.
 *
 * This could be, for example, used to check whether an ability
 * with key `ability:blink` has been populated.
 *
 * @property key the key of the ability to check with
 */
data class AbilityFilter(
    override val invert: Boolean,
    private val key: Key,
) : Filter {

    /**
     * Returns `true` if the [context] already has the ability with
     * [key][key] populated.
     */
    override fun test0(context: SchemeGenerationContext): Boolean {
        return AbilityContextHolder(key) in context.abilities
    }
}

data class AbilityContextHolder(
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
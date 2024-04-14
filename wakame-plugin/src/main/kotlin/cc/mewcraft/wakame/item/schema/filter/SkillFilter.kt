package cc.mewcraft.wakame.item.schema.filter

import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * Checks skill population.
 *
 * This could be, for example, used to check whether a skill
 * with key `skill:blink` has been populated.
 *
 * @property key the key of the skill to check with
 */
data class SkillFilter(
    override val invert: Boolean,
    private val key: Key,
) : Filter {

    /**
     * Returns `true` if the [context] already has the skill with
     * [key][key] populated.
     */
    override fun testRaw(context: SchemaGenerationContext): Boolean {
        return SkillContextHolder(key) in context.abilities
    }
}

data class SkillContextHolder(
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
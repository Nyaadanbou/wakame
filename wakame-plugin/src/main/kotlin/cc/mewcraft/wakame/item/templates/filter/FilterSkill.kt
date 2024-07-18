package cc.mewcraft.wakame.item.templates.filter

import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.templates.filter.FilterSerializer.NAMESPACE_FILTER
import cc.mewcraft.wakame.random3.Filter
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
data class FilterSkill(
    override val invert: Boolean,
    private val key: Key,
) : Filter<GenerationContext>, Examinable {
    companion object {
        val TYPE = Key.key(NAMESPACE_FILTER, "skill")
    }

    override val type: Key = TYPE

    /**
     * Returns `true` if the [context] already has the skill with
     * [key][key] populated.
     */
    override fun testOriginal(context: GenerationContext): Boolean {
        return SkillContextHolder(key) in context.skills
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

data class SkillContextHolder(
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
        if (other is SkillContextHolder)
            return key == other.key
        return false
    }
}
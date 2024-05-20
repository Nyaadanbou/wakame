package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.user.User
import com.google.common.collect.Multimap
import com.google.common.collect.MultimapBuilder
import net.kyori.adventure.key.Key
import java.util.*

/**
 * Represents a skill map owned by a subject.
 *
 * The skill map is a live object, which records the current usable skills
 * for the subject.
 */
interface SkillMap {
    fun setSkill(skillWithTrigger: ConfiguredSkillWithTrigger)
    fun setAllSkills(skillWithTriggers: Multimap<SkillTrigger, Key>)
    fun getSkills(trigger: SkillTrigger): Collection<Key>
    fun removeSkill(skillKey: Key)
    fun removeSkills(skillKeys: Collection<Key>) = skillKeys.forEach { removeSkill(it) }

    operator fun get(uniqueId: UUID, trigger: SkillTrigger): Collection<Key> = getSkills(trigger)
}

fun SkillMap.getConfiguredSkills(trigger: SkillTrigger): Collection<Skill> = getSkills(trigger).map { SkillRegistry.INSTANCE[it] }

/**
 * The no-op SkillMap. Used as placeholder code.
 */
object NoopSkillMap : SkillMap {
    override fun setSkill(skillWithTrigger: ConfiguredSkillWithTrigger) = Unit
    override fun setAllSkills(skillWithTriggers: Multimap<SkillTrigger, Key>) = Unit
    override fun getSkills(trigger: SkillTrigger): Collection<Key> = emptyList()
    override fun removeSkill(skillKey: Key) = Unit
    override fun removeSkills(skillKeys: Collection<Key>) = Unit
}

/**
 * Creates a new [PlayerSkillMap].
 */
fun PlayerSkillMap(user: User<*>): PlayerSkillMap {
    return PlayerSkillMap(user.uniqueId)
}

/**
 * This object keeps track of all activated skills owned by a player.
 *
 * It shall be used in the case where you read the input from players and
 * then check whether the input has triggered a skill or not.
 */
class PlayerSkillMap(
    private val uniqueId: UUID,
) : SkillMap {
    private val skills: Multimap<SkillTrigger, Key> = MultimapBuilder
        .hashKeys(8)
        .arrayListValues(5)
        .build()

    override fun setSkill(skillWithTrigger: ConfiguredSkillWithTrigger) {
        skills.put(skillWithTrigger.trigger, skillWithTrigger.key)
    }

    override fun setAllSkills(skillWithTriggers: Multimap<SkillTrigger, Key>) {
        skills.putAll(skillWithTriggers)
    }

    override fun getSkills(trigger: SkillTrigger): Collection<Key> {
        return skills[trigger]
    }

    override fun removeSkill(skillKey: Key) {
        skills.values().removeIf { it == skillKey }
    }

    override fun removeSkills(skillKeys: Collection<Key>) {
        skills.values().removeIf { skillKeys.contains(it) }
    }

    override fun hashCode(): Int {
        return uniqueId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlayerSkillMap) return false
        return uniqueId == other.uniqueId
    }
}
package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.user.User
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
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
    fun setSkill(trigger: SkillTrigger, skill: ConfiguredSkill) = setSkill(ConfiguredSkillWithTrigger(skill, trigger))
    fun setAllSkills(skillWithTriggers: Collection<ConfiguredSkillWithTrigger>) = skillWithTriggers.forEach { setSkill(it) }
    fun getSkills(trigger: SkillTrigger): Collection<ConfiguredSkill>
    fun removeSkill(skillKey: Key)
    fun removeSkills(skillKeys: Collection<Key>) = skillKeys.forEach { removeSkill(it) }

    operator fun set(trigger: SkillTrigger, skill: ConfiguredSkill) = setSkill(ConfiguredSkillWithTrigger(skill, trigger))
    operator fun get(uniqueId: UUID, trigger: SkillTrigger): Collection<ConfiguredSkill> = getSkills(trigger)
}

/**
 * The no-op SkillMap. Used as placeholder code.
 */
object NoopSkillMap : SkillMap {
    override fun setSkill(skillWithTrigger: ConfiguredSkillWithTrigger) = Unit
    override fun setAllSkills(skillWithTriggers: Collection<ConfiguredSkillWithTrigger>) = Unit
    override fun getSkills(trigger: SkillTrigger): Collection<ConfiguredSkill> = emptyList()
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
    private val uniqueId: UUID
) : SkillMap {
    private val skills: Multimap<SkillTrigger, ConfiguredSkill> = HashMultimap.create()

    override fun setSkill(skillWithTrigger: ConfiguredSkillWithTrigger) {
        skills.put(skillWithTrigger.trigger, skillWithTrigger.skill)
    }

    override fun getSkills(trigger: SkillTrigger): Collection<ConfiguredSkill> {
        return skills[trigger]
    }

    override fun removeSkill(skillKey: Key) {
        skills.values().removeIf { it.key == skillKey }
    }

    override fun removeSkills(skillKeys: Collection<Key>) {
        skills.values().removeIf { skillKeys.contains(it.key) }
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
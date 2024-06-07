package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill.trigger.SkillWithTrigger
import cc.mewcraft.wakame.skill.trigger.Trigger
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
    fun addSkill(skill: SkillWithTrigger)
    fun addSkillsByKey(skills: Multimap<Trigger, Key>)
    fun addSkillsByInstance(skills: Multimap<Trigger, Skill>)
    fun getSkill(trigger: Trigger): Collection<Skill>
    fun removeSkill(key: Key)
    fun removeSkill(skill: Skill)
    fun removeSkill(skills: Multimap<Trigger, Skill>)

    operator fun get(uniqueId: UUID, trigger: Trigger): Collection<Skill> = getSkill(trigger)
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
    private val skills: Multimap<Trigger, Key> = MultimapBuilder
        .hashKeys(8)
        .arrayListValues(5)
        .build()

    override fun addSkill(skill: SkillWithTrigger) {
        this.skills.put(skill.trigger, skill.key)
    }

    override fun addSkillsByKey(skills: Multimap<Trigger, Key>) {
        this.skills.putAll(skills)
    }

    override fun addSkillsByInstance(skills: Multimap<Trigger, Skill>) {
        for ((trigger, skill) in skills.entries()) {
            this.skills.put(trigger, skill.key)
        }
    }

    override fun getSkill(trigger: Trigger): Collection<Skill> {
        return this.skills[trigger].map { SkillRegistry.INSTANCE[it] }
    }

    override fun removeSkill(key: Key) {
        this.skills.entries().removeIf { it.value == key }
    }

    override fun removeSkill(skill: Skill) {
        this.skills.entries().removeIf { it.value == skill.key }
    }

    override fun removeSkill(skills: Multimap<Trigger, Skill>) {
        for ((trigger, skill) in skills.entries()) {
            this.skills.remove(trigger, skill.key)
        }
    }
}
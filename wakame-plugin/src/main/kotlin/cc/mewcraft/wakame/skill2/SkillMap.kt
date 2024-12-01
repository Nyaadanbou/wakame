package cc.mewcraft.wakame.skill2

import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill2.character.CasterAdapter
import cc.mewcraft.wakame.skill2.character.toComposite
import cc.mewcraft.wakame.skill2.context.ImmutableSkillContext
import cc.mewcraft.wakame.skill2.trigger.Trigger
import cc.mewcraft.wakame.user.PlayerAdapters
import cc.mewcraft.wakame.user.User
import com.google.common.collect.Multimap
import com.google.common.collect.MultimapBuilder
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * Represents a skill map owned by a subject.
 *
 * The skill map is a live object, which records the current usable skills
 * for the subject.
 */
interface SkillMap {
    /**
     * Adds a [ConfiguredSkill] to the skill map.
     */
    fun addSkill(skill: ConfiguredSkill)

    /**
     * Adds a collection of [Skill] keys to the skill map.
     */
    fun addSkillsByKey(skills: Multimap<Trigger, Key>)

    /**
     * Adds a collection of [Skill] instances to the skill map.
     */
    fun addSkillsByInstance(skills: Multimap<Trigger, Skill>)

    /**
     * Returns a collection of [Skill] instances that are triggered by the given [Trigger].
     */
    fun getSkill(trigger: Trigger): Collection<Skill>

    /**
     * Removes a [Skill] from the skill map by its key.
     */
    fun removeSkill(key: Key)

    /**
     * Removes a [Skill] from the skill map by its instance.
     */
    fun removeSkill(skill: Skill)

    /**
     * Removes a collection of [Skill] from the skill map.
     */
    fun removeSkill(skills: Multimap<Trigger, Skill>)

    /**
     * Returns a set of [Trigger]s that are currently available in the skill map.
     */
    fun getTriggers(): Set<Trigger>

    /**
     * Checks whether the skill map has a trigger of the given class.
     */
    fun hasTrigger(clazz: Class<out Trigger>): Boolean

    /**
     * Clears all skills in the skill map.
     *
     * 将停止所有技能的持续效果.
     */
    fun cleanup()

    /**
     * Returns a collection of [Skill] instances that are triggered by the given [Trigger].
     */
    operator fun get(uniqueId: UUID, trigger: Trigger): Collection<Skill> = getSkill(trigger)
}

inline fun <reified T : Trigger> SkillMap.hasTrigger(): Boolean {
    return hasTrigger(T::class.java)
}

fun SkillMap(user: User<Player>): SkillMap {
    return PlayerSkillMap(user.uniqueId)
}

/**
 * This object keeps track of all activated skills owned by a player.
 *
 * It shall be used in the case where you read the input from players and
 * then check whether the input has triggered a skill or not.
 */
private class PlayerSkillMap(
    private val uniqueId: UUID,
) : SkillMap {
    companion object : KoinComponent {
        private val mechanicRecorder: MechanicRecorder by inject()
    }

    private val skills: Multimap<Trigger, Key> = MultimapBuilder
        .hashKeys(8)
        .arrayListValues(5)
        .build()

    override fun addSkill(skill: ConfiguredSkill) {
        this.skills.put(skill.trigger, skill.id)
        val skillInstance = getSkillByKey(skill.id)
        registerSkillResult(skillInstance)
    }

    override fun addSkillsByKey(skills: Multimap<Trigger, Key>) {
        this.skills.putAll(skills)
        for (key in skills.values()) {
            val skillInstance = getSkillByKey(key)
            registerSkillResult(skillInstance)
        }
    }

    override fun addSkillsByInstance(skills: Multimap<Trigger, Skill>) {
        for ((trigger, skill) in skills.entries()) {
            this.skills.put(trigger, skill.key)
            registerSkillResult(skill)
        }
    }

    override fun getSkill(trigger: Trigger): Collection<Skill> {
        return this.skills[trigger].map { getSkillByKey(it) }
    }

    override fun removeSkill(key: Key) {
        this.skills.entries().removeIf { it.value == key }
        mechanicRecorder.interruptMechanic(key.asString())
    }

    override fun removeSkill(skill: Skill) {
        this.skills.entries().removeIf { it.value == skill.key }
        mechanicRecorder.interruptMechanic(skill.key.asString())
    }

    override fun removeSkill(skills: Multimap<Trigger, Skill>) {
        for ((trigger, skill) in skills.entries()) {
            this.skills.remove(trigger, skill.key)
            mechanicRecorder.interruptMechanic(skill.key.asString())
        }
    }

    override fun getTriggers(): Set<Trigger> {
        return skills.keySet()
    }

    override fun hasTrigger(clazz: Class<out Trigger>): Boolean {
        return skills.keys().any { clazz.isInstance(it) }
    }

    override fun cleanup() {
        skills.values().forEach { mechanicRecorder.interruptMechanic(it.asString()) }
        skills.clear()
    }

    private fun registerSkillResult(skill: Skill) {
        val user = PlayerAdapters.get<Player>().adapt(uniqueId)
        mechanicRecorder.addMechanic(ImmutableSkillContext(caster = CasterAdapter.adapt(user).toComposite(), skill = skill))
    }

    private fun getSkillByKey(key: Key): Skill {
        return SkillRegistry.INSTANCES[key]
    }
}
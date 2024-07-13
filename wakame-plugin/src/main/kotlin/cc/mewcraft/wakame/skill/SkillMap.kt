package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.trigger.Trigger
import cc.mewcraft.wakame.tick.Ticker
import cc.mewcraft.wakame.user.User
import com.google.common.collect.Multimap
import com.google.common.collect.MultimapBuilder
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import java.util.UUID

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
     * Returns a collection of [Skill] instances that are triggered by the given [Trigger].
     */
    operator fun get(uniqueId: UUID, trigger: Trigger): Collection<Skill> = getSkill(trigger)
}

inline fun <reified T : Trigger> SkillMap.hasTrigger(): Boolean {
    return hasTrigger(T::class.java)
}

/**
 * This object keeps track of all activated skills owned by a player.
 *
 * It shall be used in the case where you read the input from players and
 * then check whether the input has triggered a skill or not.
 */
class PlayerSkillMap(
    private val user: User<Player>
) : SkillMap {
    private val skills: Multimap<Trigger, Key> = MultimapBuilder
        .hashKeys(8)
        .arrayListValues(5)
        .build()

    private val skill2Ticks: MutableMap<Key, UUID> = Object2ObjectOpenHashMap()

    override fun addSkill(skill: ConfiguredSkill) {
        this.skills.put(skill.trigger, skill.key)
        val skillInstance = getSkillByKey(skill.key)
        registerSkillTick(skillInstance)
    }

    override fun addSkillsByKey(skills: Multimap<Trigger, Key>) {
        this.skills.putAll(skills)
        for (key in skills.values()) {
            val skillInstance = getSkillByKey(key)
            registerSkillTick(skillInstance)
        }
    }

    override fun addSkillsByInstance(skills: Multimap<Trigger, Skill>) {
        for ((trigger, skill) in skills.entries()) {
            this.skills.put(trigger, skill.key)
            registerSkillTick(skill)
        }
    }

    override fun getSkill(trigger: Trigger): Collection<Skill> {
        return this.skills[trigger].map { getSkillByKey(it) }
    }

    override fun removeSkill(key: Key) {
        this.skills.entries().removeIf { it.value == key }
        removeSkillTick(key)
    }

    override fun removeSkill(skill: Skill) {
        this.skills.entries().removeIf { it.value == skill.key }
        removeSkillTick(skill.key)
    }

    override fun removeSkill(skills: Multimap<Trigger, Skill>) {
        for ((trigger, skill) in skills.entries()) {
            this.skills.remove(trigger, skill.key)
            removeSkillTick(skill.key)
        }
    }

    override fun getTriggers(): Set<Trigger> {
        return skills.keySet()
    }

    override fun hasTrigger(clazz: Class<out Trigger>): Boolean {
        return skills.keys().any { clazz.isInstance(it) }
    }

    private fun getSkillByKey(key: Key): Skill {
        return SkillRegistry.INSTANCES[key]
    }

    private fun registerSkillTick(skill: Skill) {
        if (skill is PassiveSkill) {
            val tickable = skill.cast(SkillContext(CasterAdapter.adapt(user)))
            skill2Ticks[skill.key] = Ticker.addTick(tickable)
        }
    }

    private fun removeSkillTick(skill: Key) {
        val tickId = skill2Ticks.remove(skill)
        if (tickId != null) {
            Ticker.stopTick(tickId)
        }
    }
}
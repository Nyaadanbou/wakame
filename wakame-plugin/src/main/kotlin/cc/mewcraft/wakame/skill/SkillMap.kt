package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.user.User
import com.google.common.collect.Multimap
import com.google.common.collect.MultimapBuilder
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

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

fun SkillMap.getConfiguredSkills(trigger: SkillTrigger): Collection<ConfiguredSkill> = getSkills(trigger).map { SkillRegistry.INSTANCE[it] }

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

    private val player: Player
        get() = Bukkit.getPlayer(uniqueId)!!

    override fun setSkill(skillWithTrigger: ConfiguredSkillWithTrigger) {
        skills.put(skillWithTrigger.trigger, skillWithTrigger.key)
        player.sendMessage("Skill ${skillWithTrigger.key} is set") // TODO: Remove this line after debugging
    }

    override fun setAllSkills(skillWithTriggers: Multimap<SkillTrigger, Key>) {
        skills.putAll(skillWithTriggers)
        player.sendMessage("Skills ${skillWithTriggers.values()} are set") // TODO: Remove this line after debugging
    }

    override fun getSkills(trigger: SkillTrigger): Collection<Key> {
        player.sendMessage("Skills ${skills[trigger]} are retrieved")
        return skills[trigger]
    }

    override fun removeSkill(skillKey: Key) {
        skills.values().removeIf { it == skillKey }
        player.sendMessage("Skill $skillKey is removed") // TODO: Remove this line after debugging
    }

    override fun removeSkills(skillKeys: Collection<Key>) {
        skills.values().removeIf { skillKeys.contains(it) }
        player.sendMessage("Skills $skillKeys are removed") // TODO: Remove this line after debugging
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
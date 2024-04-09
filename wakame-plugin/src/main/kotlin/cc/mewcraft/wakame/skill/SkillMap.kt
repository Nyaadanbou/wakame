package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.user.User
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap
import net.kyori.adventure.key.Key
import java.util.UUID

/**
 * Represents a skill map owned by a subject.
 *
 * The skill map is a live object, which records the current usable skills
 * for the subject.
 */
interface SkillMap {
    fun setSkill(key: Key, skill: Skill)
    fun getSkill(key: Key): Skill?

    operator fun set(key: Key, skill: Skill) = setSkill(key, skill)
    operator fun get(key: Key): Skill? = getSkill(key)
}

/**
 * The no-op SkillMap. Used as placeholder code.
 */
object NoopSkillMap : SkillMap {
    override fun setSkill(key: Key, skill: Skill) = Unit
    override fun getSkill(key: Key): Skill? = null
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
    private val skills: MutableMap<Key, Skill> = Reference2ReferenceOpenHashMap()

    override fun setSkill(key: Key, skill: Skill) {
        skills[key] = skill
    }

    override fun getSkill(key: Key): Skill? {
        return skills[key]
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
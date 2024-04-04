package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.user.User

/**
 * Represents a skill map owned by a subject.
 *
 * The skill map is a live object, which records the current usable skills
 * for the subject.
 */
interface SkillMap

/**
 * The no-op SkillMap. Used as placeholder code.
 */
object NoopSkillMap : SkillMap

/**
 * Creates a new [PlayerSkillMap].
 */
fun PlayerSkillMap(user: User<*>): PlayerSkillMap {
    return PlayerSkillMap()
}

/**
 * This object keeps track of all activated skills owned by a player.
 *
 * It shall be used in the case where you read the input from players and
 * then check whether the input has triggered a skill or not.
 */
class PlayerSkillMap : SkillMap {

}
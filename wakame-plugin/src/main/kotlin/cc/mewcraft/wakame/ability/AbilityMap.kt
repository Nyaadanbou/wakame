package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.user.User

/**
 * Represents an Ability Map owned by a subject.
 */
interface AbilityMap

/**
 * The no-op AbilityMap. Used as placeholder code.
 */
object NoopAbilityMap : AbilityMap

/**
 * Creates a new [PlayerAbilityMap].
 */
fun PlayerAbilityMap(user: User): PlayerAbilityMap {
    return PlayerAbilityMap()
}

/**
 * This object keeps track of all activated ability owned by a player.
 *
 * It shall be used in the case where you read the input from players and
 * then check whether the input has triggered an ability or not.
 */
class PlayerAbilityMap : AbilityMap {

}
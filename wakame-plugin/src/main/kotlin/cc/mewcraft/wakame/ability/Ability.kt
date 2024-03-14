package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.registry.AbilityRegistry
import net.kyori.adventure.key.Key

/**
 * Represents an ability "attached" to a player.
 *
 * If a player has ability X, we say that the ability X is attached to that
 * player; By contrast, if the player has no ability, we say that the player
 * has no ability attached.
 */
interface Ability : Keyed

/**
 * A no-op ability. Used as placeholder object.
 */
object NoopAbility : Ability {
    override val key: Key = AbilityRegistry.EMPTY_KEY
}

/**
 * An ability that applies its effects by using specific key combinations.
 * The key combinations currently include the alternations of Mouse Left (L)
 * and Mouse Right (R), such as "RRL" and "LLR".
 */
interface ActiveAbility : Ability

/**
 * An ability that applies its effects either permanently as soon as it is
 * available, or activate by itself if the ability is available and its
 * requirements met. These requirements can range from attacking a monster,
 * casting a spell or even getting attacked.
 */
interface PassiveAbility : Ability

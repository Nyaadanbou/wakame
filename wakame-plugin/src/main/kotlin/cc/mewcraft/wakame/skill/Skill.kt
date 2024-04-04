package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.registry.SkillRegistry
import net.kyori.adventure.key.Key

/**
 * Represents a skill "attached" to a player.
 *
 * If a player has skill X, we say that the skill X is attached to that
 * player; By contrast, if the player has no skill, we say that the player
 * has no skill attached.
 */
interface Skill : Keyed {
    fun castAt(target: Target.Void) {}
    fun castAt(target: Target.Location) {}
    fun castAt(target: Target.LivingEntity) {}
}

/**
 * A no-op skill. Used as placeholder object.
 */
object NoopSkill : Skill {
    override val key: Key = SkillRegistry.EMPTY_KEY
}

/**
 * A skill that applies its effects by using specific key combinations.
 * The key combinations currently include the alternations of Mouse Left (L)
 * and Mouse Right (R), such as "RRL" and "LLR".
 */
interface ActiveSkill : Skill

/**
 * A skill that applies its effects either permanently as soon as it is
 * available, or activate by itself if the skill is available and its
 * requirements met. These requirements can range from attacking a monster,
 * casting a spell or even getting attacked.
 */
interface PassiveSkill : Skill

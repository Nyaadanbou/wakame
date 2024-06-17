package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill.condition.SkillConditionGroup
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.factory.SkillFactory
import net.kyori.adventure.key.Key

/**
 * Represents a skill "attached" to a player.
 *
 * If a player has skill X, we say that the skill X is attached to that
 * player; By contrast, if the player has no skill, we say that the player
 * has no skill attached.
 */
interface Skill : Keyed {
    /**
     * The key of this skill.
     *
     * **Note that the [key] here is specified by the location of the skill
     * configuration file, not the [SkillRegistry.FACTORIES]'s key**,
     * which means that a [SkillFactory] can have multiple [Skill].
     *
     * [Skill] will be stored in the SkillRegistry, and the corresponding
     * [Skill] will be found by the [key].
     */
    override val key: Key

    /**
     * The conditions that must be met in order to cast this skill.
     *
     * @see SkillConditionGroup
     */
    val conditions: SkillConditionGroup

    /**
     * The display infos of this skill.
     */
    val displays: SkillDisplay

    /**
     * 释放该技能.
     */
    fun cast(context: SkillCastContext): SkillCastResult = FixedSkillCastResult.NOOP
}
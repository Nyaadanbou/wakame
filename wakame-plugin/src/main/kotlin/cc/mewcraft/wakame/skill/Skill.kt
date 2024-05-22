package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.event.PlayerSkillPrepareCastEvent
import cc.mewcraft.wakame.event.SkillPrepareCastEvent
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill.condition.PlayerSkillCastContext
import cc.mewcraft.wakame.skill.condition.SkillCastContext
import cc.mewcraft.wakame.skill.condition.SkillConditionGroup
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
     * configuration file, not the [SkillRegistry.SKILL_FACTORIES]'s key**,
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
    val display: SkillDisplay

    fun cast(context: SkillCastContext) = Unit
}

fun Skill.tryCast(skillCastContext: SkillCastContext) {
    val event: SkillPrepareCastEvent
    when (skillCastContext) {
        is PlayerSkillCastContext -> {
            event = PlayerSkillPrepareCastEvent(
                this,
                skillCastContext
            )
        }

        else -> {
            return // TODO 其他释放技能的情况
        }
    }
    // 这里允许其他模块监听事件，修改上下文，从而对技能的释放产生影响
    event.callEvent()
    if (event.isCancelled) return
    val conditionGroup = this.conditions
    val context = event.skillCastContext
    if (conditionGroup.test(context)) {
        try {
            this.cast(context)
            conditionGroup.cost(context)
        } catch (e: Throwable) {
            if (e is SkillCannotCastException) {
                event.skillCastContext.caster.sendMessage(e.beautify())
            } else {
                throw e
            }
        }
    } else {
        conditionGroup.notifyFailure(context)
    }
}
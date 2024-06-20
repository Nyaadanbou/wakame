package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.event.PlayerSkillPrepareCastEvent
import cc.mewcraft.wakame.event.SkillPrepareCastEvent
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.context.SkillCastContextKey

interface SkillCastManager {
    fun tryCast(skill: Skill, context: SkillCastContext): SkillPrepareCastResult
}

internal class SkillCastManagerImpl : SkillCastManager {
    override fun tryCast(skill: Skill, context: SkillCastContext): SkillPrepareCastResult {
        val event: SkillPrepareCastEvent
        when {
            context.has(SkillCastContextKey.CASTER_PLAYER) -> {
                event = PlayerSkillPrepareCastEvent(
                    skill = skill, 
                    caster = context.get(SkillCastContextKey.CASTER_PLAYER).bukkitPlayer,
                    target = context.optional(SkillCastContextKey.TARGET),
                    item = context.optional(SkillCastContextKey.ITEM_STACK),
                )
            }

            else -> {
                return FailureSkillPrepareCastResult.UNKNOWN_FAILURE // TODO 其他释放技能的情况
            }
        }
        event.callEvent()
        if (event.isCancelled) return FailureSkillPrepareCastResult.CANCELED
        val conditionGroup = skill.conditions
        val session = conditionGroup.newSession(context)
        if (!session.isSuccess) {
            session.onFailure(context)
            return FailureSkillPrepareCastResult.CONDITION_CANNOT_MEET
        }

        try {
            val result = skill.cast(context)
            return SkillPrepareCastResult.success(result).also { session.onSuccess(context) }
        } catch (e: Throwable) {
            e.printStackTrace()
            return FailureSkillPrepareCastResult.UNKNOWN_FAILURE
        }
    }
}
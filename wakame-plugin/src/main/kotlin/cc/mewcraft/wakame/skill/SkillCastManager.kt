package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.event.PlayerSkillPrepareCastEvent
import cc.mewcraft.wakame.event.SkillPrepareCastEvent
import cc.mewcraft.wakame.skill.condition.ConditionTime
import cc.mewcraft.wakame.skill.context.SkillContext
import cc.mewcraft.wakame.skill.context.SkillContextKey

interface SkillCastManager {
    fun tryCast(skill: Skill, context: SkillContext): SkillPrepareCastResult
}

internal class SkillCastManagerImpl : SkillCastManager {
    override fun tryCast(skill: Skill, context: SkillContext): SkillPrepareCastResult {
        val event: SkillPrepareCastEvent
        when {
            context.contains(SkillContextKey.CASTER) -> {
                event = PlayerSkillPrepareCastEvent(
                    skill = skill, 
                    caster = requireNotNull(context[SkillContextKey.CASTER]?.value<Caster.Single.Player>()!!.bukkitPlayer) { "Caster is not a player" },
                    target = context[SkillContextKey.TARGET],
                    item = context[SkillContextKey.ITEM_STACK],
                )
            }

            else -> {
                return FailureSkillPrepareCastResult.UNKNOWN_FAILURE // TODO 其他释放技能的情况
            }
        }
        event.callEvent()
        if (event.isCancelled) return FailureSkillPrepareCastResult.CANCELED
        val conditionGroup = skill.conditions
        val session = conditionGroup.newSession(ConditionTime.CAST_POINT, context)
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
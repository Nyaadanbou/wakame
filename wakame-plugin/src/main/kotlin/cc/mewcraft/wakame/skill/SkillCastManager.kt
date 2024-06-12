package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.event.PlayerSkillPrepareCastEvent
import cc.mewcraft.wakame.event.SkillPrepareCastEvent
import cc.mewcraft.wakame.skill.context.SkillCastContext
import cc.mewcraft.wakame.skill.context.SkillCastContextKeys

interface SkillCastManager {
    fun tryCast(skill: Skill, skillCastContext: SkillCastContext): SkillCastResult
}

internal class SkillCastManagerImpl : SkillCastManager {
    override fun tryCast(skill: Skill, skillCastContext: SkillCastContext): SkillCastResult {
        val event: SkillPrepareCastEvent
        when {
            skillCastContext.has(SkillCastContextKeys.CASTER_PLAYER) -> {
                event = PlayerSkillPrepareCastEvent(skill, skillCastContext)
            }

            else -> {
                return SkillCastResult.SUCCESS // TODO 其他释放技能的情况
            }
        }
        // 这里允许其他模块监听事件，修改上下文，从而对技能的释放产生影响
        event.callEvent()
        if (event.isCancelled) return SkillCastResult.CANCELED
        val conditionGroup = skill.conditions
        val context = event.skillCastContext
        if (!conditionGroup.test(context)) {
            conditionGroup.notifyFailure(context)
            return SkillCastResult.CONDITION_CANNOT_MEET
        }

        try {
            val result = skill.cast(context)
            if (!result.isSuccessful())
                return result
            conditionGroup.cost(context)
            return SkillCastResult.SUCCESS
        } catch (e: Throwable) {
            e.printStackTrace()
            return SkillCastResult.UNKNOWN_FAILURE
        }
    }
}
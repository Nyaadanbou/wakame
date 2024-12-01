package cc.mewcraft.wakame.skill2

import cc.mewcraft.wakame.event.PlayerSkillPrepareCastEvent
import cc.mewcraft.wakame.event.SkillPrepareCastEvent
import cc.mewcraft.wakame.skill2.character.Caster
import cc.mewcraft.wakame.skill2.character.value
import cc.mewcraft.wakame.skill2.condition.ConditionPhase
import cc.mewcraft.wakame.skill2.context.SkillContext

interface SkillCastManager {
    fun tryCast(skill: Skill, context: SkillContext): SkillPrepareCastResult
}

internal class SkillCastManagerImpl : SkillCastManager {
    override fun tryCast(skill: Skill, context: SkillContext): SkillPrepareCastResult {
        val event: SkillPrepareCastEvent = PlayerSkillPrepareCastEvent(
            skill = skill,
            caster = requireNotNull(context.caster.value<Caster.Single.Player>()?.bukkitPlayer),
            target = context.target,
            item = context.castItem?.itemStack,
        )
        event.callEvent()
        if (event.isCancelled) return FailureSkillPrepareCastResult.CANCELED
        val conditionGroup = skill.conditions
        val session = conditionGroup.newSession(ConditionPhase.CAST_POINT, context)
        if (!session.isSuccess) {
            session.onFailure(context)
            return FailureSkillPrepareCastResult.CONDITION_CANNOT_MEET
        }

        try {
            val result = skill.result(context)
            return SkillPrepareCastResult.success(result).also { session.onSuccess(context) }
        } catch (e: Throwable) {
            e.printStackTrace()
            return FailureSkillPrepareCastResult.UNKNOWN_FAILURE
        }
    }
}
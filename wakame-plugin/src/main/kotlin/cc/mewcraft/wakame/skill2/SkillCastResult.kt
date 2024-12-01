package cc.mewcraft.wakame.skill2

import cc.mewcraft.wakame.skill2.result.SkillResult
import net.kyori.adventure.translation.Translatable

interface SkillPrepareCastResult {
    val skillResult: SkillResult<*>

    fun isSuccessful(): Boolean

    companion object {
        fun success(skillResult: SkillResult<*>): SkillPrepareCastResult {
            return SuccessSkillPrepareCastResult(skillResult)
        }
    }
}

private data class SuccessSkillPrepareCastResult(
    override val skillResult: SkillResult<*>
) : SkillPrepareCastResult {

    override fun isSuccessful(): Boolean {
        return true
    }
}

enum class FailureSkillPrepareCastResult(
    private val translateKey: String,
) : SkillPrepareCastResult, Translatable {
    CANCELED("skill.result.canceled"),
    CONDITION_CANNOT_MEET("skill.result.condition_cannot_meet"),
    UNKNOWN_FAILURE("skill.result.unknown_failure"),
    ;

    override val skillResult: SkillResult<*> = SkillResult()

    override fun translationKey(): String = translateKey

    override fun isSuccessful(): Boolean {
        return false
    }
}
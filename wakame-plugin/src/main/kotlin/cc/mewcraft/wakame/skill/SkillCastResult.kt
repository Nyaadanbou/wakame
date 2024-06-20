package cc.mewcraft.wakame.skill

import net.kyori.adventure.translation.Translatable

interface SkillPrepareCastResult {
    val skillTick: SkillTick

    fun isSuccessful(): Boolean

    companion object {
        fun success(skillTick: SkillTick): SkillPrepareCastResult {
            return SuccessSkillPrepareCastResult(skillTick)
        }
    }
}

private data class SuccessSkillPrepareCastResult(
    override val skillTick: SkillTick
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

    override val skillTick: SkillTick = SkillTick.empty()

    override fun translationKey(): String = translateKey

    override fun isSuccessful(): Boolean {
        return false
    }
}
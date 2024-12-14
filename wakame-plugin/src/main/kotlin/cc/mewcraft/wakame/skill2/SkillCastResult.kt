package cc.mewcraft.wakame.skill2

import cc.mewcraft.wakame.skill2.result.SkillMechanic
import net.kyori.adventure.translation.Translatable

interface SkillPrepareCastResult {
    val skillMechanic: SkillMechanic<*>

    fun isSuccessful(): Boolean

    companion object {
        fun success(skillResult: SkillMechanic<*>): SkillPrepareCastResult {
            return SuccessSkillPrepareCastResult(skillResult)
        }
    }
}

private data class SuccessSkillPrepareCastResult(
    override val skillMechanic: SkillMechanic<*>
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

    override val skillMechanic: SkillMechanic<*> = SkillMechanic()

    override fun translationKey(): String = translateKey

    override fun isSuccessful(): Boolean {
        return false
    }
}
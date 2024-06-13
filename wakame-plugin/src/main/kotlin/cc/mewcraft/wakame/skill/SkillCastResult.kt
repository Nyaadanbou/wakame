package cc.mewcraft.wakame.skill

import net.kyori.adventure.translation.Translatable

interface SkillCastResult : Translatable {
    fun isSuccessful(): Boolean
}

enum class FixedSkillCastResult(
    private val translateKey: String,
) : SkillCastResult {
    SUCCESS("skill.result.success"),
    NONE_CASTER("skill.result.none_caster"),
    NONE_TARGET("skill.result.none_target"),
    CANCELED("skill.result.canceled"),
    CONDITION_CANNOT_MEET("skill.result.condition_cannot_meet"),
    NOOP("skill.result.noop"),
    UNKNOWN_FAILURE("skill.result.unknown_failure");

    override fun translationKey(): String = translateKey

    override fun isSuccessful(): Boolean {
        return this == SUCCESS
    }
}
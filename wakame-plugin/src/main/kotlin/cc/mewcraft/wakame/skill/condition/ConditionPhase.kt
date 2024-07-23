package cc.mewcraft.wakame.skill.condition

/**
 * 表示 [SkillCondition] 判断的时机.
 */
enum class ConditionPhase {
    /**
     * 在技能释放前判断.
     */
    CAST_POINT,

    /**
     * 在技能释放之间判断.
     *
     * 会在技能每 tick 一次时调用, 不满足则会完成技能.
     */
    CASTING,
}
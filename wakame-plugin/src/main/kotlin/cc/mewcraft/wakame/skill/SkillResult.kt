package cc.mewcraft.wakame.skill

/**
 * 代表了一个技能执行的结果.
 */
interface SkillResult<out S : Skill> {
    /**
     * 产生次结果的技能.
     */
    val skill: S

    /**
     * 执行此技能施法前摇逻辑.
     */
    fun executeCastPoint() = Unit

    /**
     * 执行此技能的施法时逻辑.
     */
    fun executeCast() = Unit

    /**
     * 执行此技能施法后摇逻辑
     */
    fun executeBackswing() = Unit
}

fun SkillResult(): SkillResult<Skill> {
    return EmptySkillResult
}

private data object EmptySkillResult : SkillResult<Skill> {
    override val skill: Skill = Skill.empty()
}
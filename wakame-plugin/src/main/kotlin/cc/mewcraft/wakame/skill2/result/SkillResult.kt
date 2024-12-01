package cc.mewcraft.wakame.skill2.result

import cc.mewcraft.wakame.ecs.Result
import cc.mewcraft.wakame.ecs.external.ComponentMap
import cc.mewcraft.wakame.skill2.Skill
import cc.mewcraft.wakame.skill2.context.SkillContext

/**
 * 代表了一个技能执行的结果.
 */
interface SkillResult<out S : Skill> : Result {
    /**
     * 产生此结果的技能上下文.
     */
    val context: SkillContext

    override fun tick(tickCount: Double, componentMap: ComponentMap) {
        // TODO: 获取 state 并且根据 state 调用对应方法
        // val state =

        tickCast(tickCount, componentMap)
    }

    /**
     * 执行此技能施法前摇逻辑.
     */
    fun tickCastPoint(tickCount: Double, componentMap: ComponentMap) = Unit

    /**
     * 执行此技能的施法时逻辑.
     */
    fun tickCast(tickCount: Double, componentMap: ComponentMap) = Unit

    /**
     * 执行此技能施法后摇逻辑
     */
    fun tickBackswing(tickCount: Double, componentMap: ComponentMap) = Unit

    /**
     * 是否是空的执行逻辑.
     */
    fun isEmpty(): Boolean = true
}

fun SkillResult(): SkillResult<Skill> {
    return EmptySkillResult
}

private data object EmptySkillResult : SkillResult<Skill> {
    override val context: SkillContext = throw UnsupportedOperationException("Empty SkillResult doesn't have context")
}
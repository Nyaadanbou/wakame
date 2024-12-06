package cc.mewcraft.wakame.skill2.result

import cc.mewcraft.wakame.ecs.Result
import cc.mewcraft.wakame.ecs.data.TickResult
import cc.mewcraft.wakame.ecs.external.ComponentMap
import cc.mewcraft.wakame.skill2.Skill
import cc.mewcraft.wakame.ecs.component.StatePhaseComponent
import cc.mewcraft.wakame.skill2.context.SkillContext
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.skill2.state.exception.IllegalSkillStateException
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.superclasses

/**
 * 代表了一个技能执行的结果.
 */
interface SkillResult<out S : Skill> : Result {
    /**
     * 产生此结果的技能上下文.
     */
    val context: SkillContext

    override fun tick(deltaTime: Double, tickCount: Double, componentMap: ComponentMap): TickResult {
        try {
            val state = componentMap[StatePhaseComponent]
            if (state == null) {
                return tickCast(deltaTime, tickCount, componentMap)
            }

            return when (state.phase) {
                StatePhase.IDLE -> tickIdle(deltaTime, tickCount, componentMap)
                StatePhase.CAST_POINT -> tickCastPoint(deltaTime, tickCount, componentMap)
                StatePhase.CASTING -> tickCast(deltaTime, tickCount, componentMap)
                StatePhase.BACKSWING -> tickBackswing(deltaTime, tickCount, componentMap)
            }
        } catch (t: Throwable) {
            val skillKClass = context.skill::class
            val skillName = skillKClass.superclasses.first { it.isSubclassOf(Skill::class) }.simpleName ?: skillKClass.simpleName
            throw IllegalSkillStateException("在执行 $skillName 技能时发生了异常", t)
        }
    }

    /**
     * 一般不会在 [StatePhase.IDLE] 中进行状态转换, 这部分逻辑交给 [cc.mewcraft.wakame.skill2.state.IdleStateInfo].
     */
    fun tickIdle(deltaTime: Double, tickCount: Double, componentMap: ComponentMap): TickResult = TickResult.ALL_DONE

    /**
     * 执行此技能施法前摇逻辑.
     */
    fun tickCastPoint(deltaTime: Double, tickCount: Double, componentMap: ComponentMap): TickResult = TickResult.ALL_DONE

    /**
     * 执行此技能的施法时逻辑.
     */
    fun tickCast(deltaTime: Double, tickCount: Double, componentMap: ComponentMap): TickResult = TickResult.ALL_DONE

    /**
     * 执行此技能施法后摇逻辑
     */
    fun tickBackswing(deltaTime: Double, tickCount: Double, componentMap: ComponentMap): TickResult = TickResult.ALL_DONE

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
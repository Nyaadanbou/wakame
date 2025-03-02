package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.ecs.Mechanic
import cc.mewcraft.wakame.ecs.component.AbilityComponent
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.Tags
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.ecs.data.TickResult
import cc.mewcraft.wakame.ecs.external.ComponentBridge

/**
 * 代表了一个主动技能执行的机制.
 */
abstract class ActiveAbilityMechanic : Mechanic {

    /**
     * 一般不会在 [StatePhase.IDLE] 中直接进行状态转换, 这部分逻辑交给 [cc.mewcraft.wakame.ability.state.PlayerComboInfo].
     *
     * @see cc.mewcraft.wakame.ability.state.PlayerComboInfo
     */
    open fun tickIdle(deltaTime: Double, tickCount: Double, componentBridge: ComponentBridge): TickResult {
        // 默认将技能标记为准备移除.
        componentBridge += Tags.READY_TO_REMOVE
        return TickResult.CONTINUE_TICK
    }

    /**
     * 执行此技能施法前摇逻辑.
     */
    open fun tickCastPoint(deltaTime: Double, tickCount: Double, componentBridge: ComponentBridge): TickResult = TickResult.NEXT_STATE_NO_CONSUME

    /**
     * 执行此技能的施法时逻辑.
     */
    open fun tickCast(deltaTime: Double, tickCount: Double, componentBridge: ComponentBridge): TickResult = TickResult.NEXT_STATE_NO_CONSUME

    /**
     * 执行此技能施法后摇逻辑
     */
    open fun tickBackswing(deltaTime: Double, tickCount: Double, componentBridge: ComponentBridge): TickResult = TickResult.NEXT_STATE_NO_CONSUME

    /**
     * 执行此技能的重置逻辑.
     */
    open fun tickReset(deltaTime: Double, tickCount: Double, componentBridge: ComponentBridge): TickResult = TickResult.NEXT_STATE_NO_CONSUME

    /**
     * 是否是空的执行逻辑.
     */
    fun isEmpty(): Boolean = true

    final override fun tick(deltaTime: Double, tickCount: Double, componentBridge: ComponentBridge): TickResult {
        try {
            val state = componentBridge[AbilityComponent]
            if (state == null) {
                throw IllegalStateException("技能实体缺少 AbilityComponent 组件")
            }

            return when (state.phase) {
                StatePhase.IDLE -> tickIdle(deltaTime, tickCount, componentBridge)
                StatePhase.CAST_POINT -> tickCastPoint(deltaTime, tickCount, componentBridge)
                StatePhase.CASTING -> tickCast(deltaTime, tickCount, componentBridge)
                StatePhase.BACKSWING -> tickBackswing(deltaTime, tickCount, componentBridge)
                StatePhase.RESET -> tickReset(deltaTime, tickCount, componentBridge)
            }
        } catch (t: Throwable) {
            val abilityName = componentBridge[IdentifierComponent]?.id ?: "未知技能"
            throw IllegalStateException("在执行 $abilityName 技能时发生了异常", t)
        }
    }
}
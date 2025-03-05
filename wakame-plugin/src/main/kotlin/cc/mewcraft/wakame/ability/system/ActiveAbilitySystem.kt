package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ecs.component.AbilityComponent
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.Tags
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.ecs.data.TickResult
import cc.mewcraft.wakame.ecs.external.ComponentBridge

interface ActiveAbilitySystem {
    /**
     * 一般不会在 [StatePhase.IDLE] 中直接进行状态转换, 这部分逻辑交给 [cc.mewcraft.wakame.ability.state.PlayerComboInfo].
     *
     * @see cc.mewcraft.wakame.ability.state.PlayerComboInfo
     */
    fun tickIdle(deltaTime: Double, tickCount: Double, componentBridge: ComponentBridge): TickResult {
        // 默认将技能标记为准备移除.
        componentBridge += Tags.READY_TO_REMOVE
        return TickResult.CONTINUE_TICK
    }

    /**
     * 执行此技能施法前摇逻辑.
     */
    fun tickCastPoint(deltaTime: Double, tickCount: Double, componentBridge: ComponentBridge): TickResult = TickResult.NEXT_STATE_NO_CONSUME

    /**
     * 执行此技能的施法时逻辑.
     */
    fun tickCast(deltaTime: Double, tickCount: Double, componentBridge: ComponentBridge): TickResult = TickResult.NEXT_STATE_NO_CONSUME

    /**
     * 执行此技能施法后摇逻辑
     */
    fun tickBackswing(deltaTime: Double, tickCount: Double, componentBridge: ComponentBridge): TickResult = TickResult.NEXT_STATE_NO_CONSUME

    /**
     * 执行此技能的重置逻辑.
     */
    fun tickReset(deltaTime: Double, tickCount: Double, componentBridge: ComponentBridge): TickResult = TickResult.NEXT_STATE_NO_CONSUME

    fun tick(deltaTime: Double, tickCount: Double, componentBridge: ComponentBridge): TickResult {
        try {
            val state = componentBridge[AbilityComponent]

            return when (state.phase) {
                StatePhase.IDLE -> tickIdle(deltaTime, tickCount, componentBridge)
                StatePhase.CAST_POINT -> tickCastPoint(deltaTime, tickCount, componentBridge)
                StatePhase.CASTING -> tickCast(deltaTime, tickCount, componentBridge)
                StatePhase.BACKSWING -> tickBackswing(deltaTime, tickCount, componentBridge)
                StatePhase.RESET -> tickReset(deltaTime, tickCount, componentBridge)
            }
        } catch (t: Throwable) {
            val abilityName = componentBridge[IdentifierComponent].id
            throw IllegalStateException("在执行 $abilityName 技能时发生了异常", t)
        }
    }
}
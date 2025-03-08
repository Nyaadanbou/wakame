package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ecs.bridge.FleksEntity
import cc.mewcraft.wakame.ecs.component.AbilityComponent
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.ecs.data.TickResult
import com.github.quillraven.fleks.EntityUpdateContext

interface ActiveAbilitySystem {

    /**
     * 一般不会在 [StatePhase.IDLE] 中直接进行状态转换, 这部分逻辑交给 [cc.mewcraft.wakame.ability.state.PlayerComboInfo].
     *
     * @see cc.mewcraft.wakame.ability.state.PlayerComboInfo
     */
    context(EntityUpdateContext)
    fun tickIdle(tickCount: Double, fleksEntity: FleksEntity): TickResult {
        // 默认将技能标记为准备移除.
        fleksEntity[AbilityComponent].isReadyToRemove = true
        return TickResult.CONTINUE_TICK
    }

    /**
     * 执行此技能施法前摇逻辑.
     */
    context(EntityUpdateContext)
    fun tickCastPoint(tickCount: Double, fleksEntity: FleksEntity): TickResult =
        TickResult.NEXT_STATE_NO_CONSUME

    /**
     * 执行此技能的施法时逻辑.
     */
    context(EntityUpdateContext)
    fun tickCast(tickCount: Double, fleksEntity: FleksEntity): TickResult =
        TickResult.NEXT_STATE_NO_CONSUME

    /**
     * 执行此技能施法后摇逻辑
     */
    context(EntityUpdateContext)
    fun tickBackswing(tickCount: Double, fleksEntity: FleksEntity): TickResult =
        TickResult.NEXT_STATE_NO_CONSUME

    /**
     * 执行此技能的重置逻辑.
     */
    context(EntityUpdateContext)
    fun tickReset(tickCount: Double, fleksEntity: FleksEntity): TickResult =
        TickResult.NEXT_STATE_NO_CONSUME

    context(EntityUpdateContext)
    fun tick(tickCount: Double, fleksEntity: FleksEntity): TickResult {
        try {
            val state = fleksEntity[AbilityComponent]
            var tickResult = TickResult.CONTINUE_TICK
            fleksEntity.configure {
                tickResult = when (state.phase) {
                    StatePhase.IDLE -> tickIdle(tickCount, fleksEntity)
                    StatePhase.CAST_POINT -> tickCastPoint(tickCount, fleksEntity)
                    StatePhase.CASTING -> tickCast(tickCount, fleksEntity)
                    StatePhase.BACKSWING -> tickBackswing(tickCount, fleksEntity)
                    StatePhase.RESET -> tickReset(tickCount, fleksEntity)
                }
            }

            return tickResult
        } catch (t: Throwable) {
            val abilityName = fleksEntity[IdentifierComponent].id
            throw IllegalStateException("在执行 $abilityName 技能时发生了异常", t)
        }
    }
}
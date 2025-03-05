package cc.mewcraft.wakame.ability.system

import cc.mewcraft.wakame.ecs.component.AbilityComponent
import cc.mewcraft.wakame.ecs.component.IdentifierComponent
import cc.mewcraft.wakame.ecs.component.Tags
import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.ecs.data.TickResult
import cc.mewcraft.wakame.ecs.external.KoishEntity

interface ActiveAbilitySystem {
    /**
     * 一般不会在 [StatePhase.IDLE] 中直接进行状态转换, 这部分逻辑交给 [cc.mewcraft.wakame.ability.state.PlayerComboInfo].
     *
     * @see cc.mewcraft.wakame.ability.state.PlayerComboInfo
     */
    fun tickIdle(deltaTime: Double, tickCount: Double, koishEntity: KoishEntity): TickResult {
        // 默认将技能标记为准备移除.
        koishEntity += Tags.READY_TO_REMOVE
        return TickResult.CONTINUE_TICK
    }

    /**
     * 执行此技能施法前摇逻辑.
     */
    fun tickCastPoint(deltaTime: Double, tickCount: Double, koishEntity: KoishEntity): TickResult = TickResult.NEXT_STATE_NO_CONSUME

    /**
     * 执行此技能的施法时逻辑.
     */
    fun tickCast(deltaTime: Double, tickCount: Double, koishEntity: KoishEntity): TickResult = TickResult.NEXT_STATE_NO_CONSUME

    /**
     * 执行此技能施法后摇逻辑
     */
    fun tickBackswing(deltaTime: Double, tickCount: Double, koishEntity: KoishEntity): TickResult = TickResult.NEXT_STATE_NO_CONSUME

    /**
     * 执行此技能的重置逻辑.
     */
    fun tickReset(deltaTime: Double, tickCount: Double, koishEntity: KoishEntity): TickResult = TickResult.NEXT_STATE_NO_CONSUME

    fun tick(deltaTime: Double, tickCount: Double, koishEntity: KoishEntity): TickResult {
        try {
            val state = koishEntity[AbilityComponent]

            return when (state.phase) {
                StatePhase.IDLE -> tickIdle(deltaTime, tickCount, koishEntity)
                StatePhase.CAST_POINT -> tickCastPoint(deltaTime, tickCount, koishEntity)
                StatePhase.CASTING -> tickCast(deltaTime, tickCount, koishEntity)
                StatePhase.BACKSWING -> tickBackswing(deltaTime, tickCount, koishEntity)
                StatePhase.RESET -> tickReset(deltaTime, tickCount, koishEntity)
            }
        } catch (t: Throwable) {
            val abilityName = koishEntity[IdentifierComponent].id
            throw IllegalStateException("在执行 $abilityName 技能时发生了异常", t)
        }
    }
}
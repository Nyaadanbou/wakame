package cc.mewcraft.wakame.ability2.system

import cc.mewcraft.wakame.ability2.component.AbilityComponent
import cc.mewcraft.wakame.ability2.StatePhase
import cc.mewcraft.wakame.ability2.TickResult
import cc.mewcraft.wakame.ecs.bridge.FleksEntity
import cc.mewcraft.wakame.registry2.KoishRegistries2
import com.github.quillraven.fleks.EntityUpdateContext

interface ActiveAbilitySystem {

    /**
     * 一般不会在技能中直接进行 [StatePhase.IDLE] 的转换.
     */
    context(EntityUpdateContext)
    fun tickIdle(tickCount: Int, entity: FleksEntity): TickResult {
        // 默认将技能标记为准备移除.
        entity[AbilityComponent].isReadyToRemove = true
        return TickResult.CONTINUE_TICK
    }

    /**
     * 执行此技能施法前摇逻辑.
     */
    context(EntityUpdateContext)
    fun tickCastPoint(tickCount: Int, entity: FleksEntity): TickResult =
        TickResult.ADVANCE_TO_NEXT_STATE_NO_CONSUME

    /**
     * 执行此技能的施法时逻辑.
     */
    context(EntityUpdateContext)
    fun tickCast(tickCount: Int, entity: FleksEntity): TickResult =
        TickResult.ADVANCE_TO_NEXT_STATE_NO_CONSUME

    /**
     * 执行此技能施法后摇逻辑
     */
    context(EntityUpdateContext)
    fun tickBackswing(tickCount: Int, entity: FleksEntity): TickResult =
        TickResult.ADVANCE_TO_NEXT_STATE_NO_CONSUME

    /**
     * 执行此技能的重置逻辑.
     */
    context(EntityUpdateContext)
    fun tickReset(tickCount: Int, entity: FleksEntity): TickResult =
        TickResult.ADVANCE_TO_NEXT_STATE_NO_CONSUME

    context(EntityUpdateContext)
    fun tick(tickCount: Int, entity: FleksEntity): TickResult {
        try {
            val state = entity[AbilityComponent]
            var tickResult = TickResult.CONTINUE_TICK
            entity.configure {
                tickResult = when (state.phase) {
                    StatePhase.IDLE -> tickIdle(tickCount, entity)
                    StatePhase.CAST_POINT -> tickCastPoint(tickCount, entity)
                    StatePhase.CASTING -> tickCast(tickCount, entity)
                    StatePhase.BACKSWING -> tickBackswing(tickCount, entity)
                    StatePhase.RESET -> tickReset(tickCount, entity)
                }
            }

            return tickResult
        } catch (t: Throwable) {
            val abilityName = KoishRegistries2.ABILITY_META_TYPE.getKey(entity[AbilityComponent].metaType) ?: "Unknown"
            throw IllegalStateException("在执行 $abilityName 技能时发生了异常", t)
        }
    }
}
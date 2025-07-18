package cc.mewcraft.wakame.ability2.system

import cc.mewcraft.wakame.ability2.StatePhase
import cc.mewcraft.wakame.ability2.component.Ability
import cc.mewcraft.wakame.ecs.bridge.EEntity
import cc.mewcraft.wakame.ecs.get
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import com.github.quillraven.fleks.EntityUpdateContext

interface AbilitySkeleton {

    /**
     * 一般不会在技能中直接进行 [StatePhase.Idle] 的转换.
     *
     * @return 下一 tick 的状态.
     */
    context(_: EntityUpdateContext)
    fun tickIdle(tickCount: Int, entity: EEntity): StatePhase {
        // 默认将技能标记为准备移除.
        entity[Ability].isReadyToRemove = true
        return StatePhase.Idle()
    }

    /**
     * 执行此技能施法前摇逻辑.
     *
     * @return 下一 tick 的状态.
     */
    context(_: EntityUpdateContext)
    fun tickCastPoint(tickCount: Int, entity: EEntity): StatePhase =
        StatePhase.Casting()

    /**
     * 执行此技能的施法时逻辑.
     *
     * @return 下一 tick 的状态.
     */
    context(_: EntityUpdateContext)
    fun tickCast(tickCount: Int, entity: EEntity): StatePhase =
        StatePhase.Backswing()

    /**
     * 执行此技能施法后摇逻辑
     */
    context(_: EntityUpdateContext)
    fun tickBackswing(tickCount: Int, entity: EEntity): StatePhase =
        StatePhase.Reset()

    /**
     * 执行此技能的重置逻辑.
     */
    context(_: EntityUpdateContext)
    fun tickReset(tickCount: Int, entity: EEntity): StatePhase =
        StatePhase.Idle()

    context(_: EntityUpdateContext)
    fun tick(tickCount: Int, phase: StatePhase, entity: EEntity): StatePhase {
        try {
            return when (phase) {
                is StatePhase.Idle -> tickIdle(tickCount, entity)
                is StatePhase.CastPoint -> tickCastPoint(tickCount, entity)
                is StatePhase.Casting -> tickCast(tickCount, entity)
                is StatePhase.Backswing -> tickBackswing(tickCount, entity)
                is StatePhase.Reset -> tickReset(tickCount, entity)
            }
        } catch (t: Throwable) {
            val abilityName = BuiltInRegistries.ABILITY_META_TYPE.getKey(entity[Ability].meta.type) ?: "Unknown"
            throw IllegalStateException("在执行 $abilityName 技能的阶段 $phase 时发生了异常", t)
        }
    }
}
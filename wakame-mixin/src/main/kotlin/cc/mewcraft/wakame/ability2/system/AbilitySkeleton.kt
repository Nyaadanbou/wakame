package cc.mewcraft.wakame.ability2.system

import cc.mewcraft.wakame.ability2.StatePhase
import cc.mewcraft.wakame.ability2.component.Ability
import cc.mewcraft.wakame.ecs.bridge.EEntity
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import com.github.quillraven.fleks.EntityUpdateContext

interface AbilitySkeleton {

    /**
     * 一般不会在技能中直接进行 [StatePhase.Idle] 的转换.
     *
     * @return 下一 tick 的状态.
     */
    context(EntityUpdateContext)
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
    context(EntityUpdateContext)
    fun tickCastPoint(tickCount: Int, entity: EEntity): StatePhase =
        StatePhase.Casting()

    /**
     * 执行此技能的施法时逻辑.
     *
     * @return 下一 tick 的状态.
     */
    context(EntityUpdateContext)
    fun tickCast(tickCount: Int, entity: EEntity): StatePhase =
        StatePhase.Backswing()

    /**
     * 执行此技能施法后摇逻辑
     */
    context(EntityUpdateContext)
    fun tickBackswing(tickCount: Int, entity: EEntity): StatePhase =
        StatePhase.Reset()

    /**
     * 执行此技能的重置逻辑.
     */
    context(EntityUpdateContext)
    fun tickReset(tickCount: Int, entity: EEntity): StatePhase =
        StatePhase.Idle()

    context(EntityUpdateContext)
    fun tick(tickCount: Int, entity: EEntity): StatePhase {
        try {
            val ability = entity[Ability]
            val nextPhase = when (ability.phase) {
                is StatePhase.Idle -> tickIdle(tickCount, entity)
                is StatePhase.CastPoint -> tickCastPoint(tickCount, entity)
                is StatePhase.Casting -> tickCast(tickCount, entity)
                is StatePhase.Backswing -> tickBackswing(tickCount, entity)
                is StatePhase.Reset -> tickReset(tickCount, entity)
            }

            return nextPhase
        } catch (t: Throwable) {
            val abilityName = BuiltInRegistries.ABILITY_META_TYPE.getKey(entity[Ability].metaType) ?: "Unknown"
            throw IllegalStateException("在执行 $abilityName 技能时发生了异常", t)
        }
    }
}
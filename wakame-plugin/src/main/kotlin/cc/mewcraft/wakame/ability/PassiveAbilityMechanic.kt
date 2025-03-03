package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ecs.Mechanic
import cc.mewcraft.wakame.ecs.component.Tags
import cc.mewcraft.wakame.ecs.data.TickResult
import cc.mewcraft.wakame.ecs.external.ComponentBridge

/**
 * 代表一个被动技能的 [Mechanic].
 */
abstract class PassiveAbilityMechanic : Mechanic {
    /**
     * 被动技能的 tick 方法.
     */
    abstract fun passiveTick(deltaTime: Double, tickCount: Double, componentBridge: ComponentBridge)

    final override fun tick(deltaTime: Double, tickCount: Double, componentBridge: ComponentBridge): TickResult {
        componentBridge += Tags.READY_TO_REMOVE
        try {
            passiveTick(deltaTime, tickCount, componentBridge)
        } catch (e: Exception) {
            LOGGER.error("Error in passive ability tick", e)
            return TickResult.INTERRUPT
        }
        return TickResult.CONTINUE_TICK
    }
}
package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.ecs.Mechanic
import cc.mewcraft.wakame.ecs.component.Tags
import cc.mewcraft.wakame.ecs.data.TickResult
import cc.mewcraft.wakame.ecs.external.ComponentBridge

/**
 * 代表一个被动技能的 [Mechanic].
 */
abstract class PassiveAbilityMechanic : Mechanic {
    override fun tick(deltaTime: Double, tickCount: Double, componentBridge: ComponentBridge): TickResult {
        componentBridge += Tags.READY_TO_REMOVE
        return TickResult.CONTINUE_TICK
    }
}
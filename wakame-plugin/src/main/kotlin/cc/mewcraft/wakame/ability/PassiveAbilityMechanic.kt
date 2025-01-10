package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.ecs.Mechanic
import cc.mewcraft.wakame.ecs.component.Tags
import cc.mewcraft.wakame.ecs.data.TickResult
import cc.mewcraft.wakame.ecs.external.ComponentMap

abstract class PassiveAbilityMechanic : Mechanic {
    override fun tick(deltaTime: Double, tickCount: Double, componentMap: ComponentMap): TickResult {
        componentMap += Tags.READY_TO_REMOVE
        return TickResult.CONTINUE_TICK
    }
}
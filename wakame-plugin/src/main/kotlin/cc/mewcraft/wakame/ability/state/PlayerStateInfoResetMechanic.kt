@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ability.state

import cc.mewcraft.wakame.ecs.Mechanic
import cc.mewcraft.wakame.ecs.data.TickResult
import cc.mewcraft.wakame.ecs.external.ComponentMap

internal class PlayerStateInfoResetMechanic(
    private val stateInfo: PlayerStateInfo
) : Mechanic {
    override fun tick(deltaTime: Double, tickCount: Double, componentMap: ComponentMap): TickResult {
        if (tickCount <= 40) {
            return TickResult.CONTINUE_TICK
        }
        return TickResult.ALL_DONE
    }

    override fun onDisable(componentMap: ComponentMap) {
        stateInfo.clearSequence()
    }
}
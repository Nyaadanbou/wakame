@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ability.state

import cc.mewcraft.wakame.ecs.Mechanic
import cc.mewcraft.wakame.ecs.data.TickResult
import cc.mewcraft.wakame.ecs.external.KoishEntity

internal class PlayerStateInfoResetMechanic(
    private val stateInfo: PlayerComboInfo
) : Mechanic {
    override fun tick(deltaTime: Double, tickCount: Double, koishEntity: KoishEntity): TickResult {
        if (tickCount <= 40) {
            return TickResult.CONTINUE_TICK
        }
        return TickResult.ALL_DONE
    }

    override fun onDispose(koishEntity: KoishEntity) {
        stateInfo.clearSequence()
    }
}
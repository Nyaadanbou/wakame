@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.ability.state

import cc.mewcraft.wakame.ability.trigger.SingleTrigger
import cc.mewcraft.wakame.ecs.Mechanic
import cc.mewcraft.wakame.ecs.data.TickResult
import cc.mewcraft.wakame.ecs.external.ComponentMap
import me.lucko.helper.Events
import me.lucko.helper.event.Subscription
import org.bukkit.Input
import org.bukkit.event.player.PlayerInputEvent

internal class PlayerStateInfoMechanic(
    private val stateInfo: PlayerStateInfo
) : Mechanic {
    private lateinit var inputSubscription: Subscription

    private fun Input.toTrigger(): SingleTrigger {
        return when {
            this.isJump -> SingleTrigger.JUMP
            this.isSneak -> SingleTrigger.SNEAK
            else -> SingleTrigger.MOVE
        }
    }

    override fun onEnable(componentMap: ComponentMap) {
        inputSubscription = Events.subscribe(PlayerInputEvent::class.java)
            .filter { it.player == stateInfo.player }
            .handler { event ->
                val input = event.input
                stateInfo.addTrigger(input.toTrigger())
            }
    }

    override fun tick(deltaTime: Double, tickCount: Double, componentMap: ComponentMap): TickResult {
        if (tickCount <= 40) {
            return TickResult.CONTINUE_TICK
        }
        return TickResult.ALL_DONE
    }

    override fun onDisable(componentMap: ComponentMap) {
        inputSubscription.unregister()
        stateInfo.clearSequence()
    }
}
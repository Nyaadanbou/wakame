package cc.mewcraft.wakame.ecs

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class EcsListener(
    private val world: WakameWorld
) : Listener {
    @EventHandler
    private fun on(e: ServerTickEndEvent) {
        world.tick()
    }
}
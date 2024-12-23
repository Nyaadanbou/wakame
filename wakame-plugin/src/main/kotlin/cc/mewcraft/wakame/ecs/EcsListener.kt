package cc.mewcraft.wakame.ecs

import com.destroystokyo.paper.event.server.ServerTickStartEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class EcsListener(
    private val world: WakameWorld
) : Listener {
    /*
       开发日记 24/12/14

       在这里使用 ServerTickEndEvent 的时候, tick 函数内调用 Player#setVelocity 时,
       玩家的加速度会被服务端的内部逻辑覆盖, 导致玩家会在瞬移到空中后自由落体.
       把这里改成 ServerTickStartEvent, 让服务端逻辑在这里之后执行, 就解决了这个问题.
     */

    @EventHandler
    private fun on(e: ServerTickStartEvent) {
        world.tick()
    }
}
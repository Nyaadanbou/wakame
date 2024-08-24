package cc.mewcraft.wakame.pack

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * 负责监听玩家加入服务器事件, 并发送资源包给玩家.
 */
internal class ResourcePackPlayerListener : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val service = ResourcePackServiceProvider.get()

        service.sendPack(player)
    }
}
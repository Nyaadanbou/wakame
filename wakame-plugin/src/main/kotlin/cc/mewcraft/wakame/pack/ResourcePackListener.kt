package cc.mewcraft.wakame.pack

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.koin.core.component.KoinComponent

class ResourcePackListener : Listener, KoinComponent {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        ResourcePackFacade.service.sendToPlayer(player)
    }
}
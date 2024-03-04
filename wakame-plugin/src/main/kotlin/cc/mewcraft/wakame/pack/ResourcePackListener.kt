package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.pack.generate.ResourcePackManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ResourcePackListener : Listener, KoinComponent {
    private val resourcePackManager: ResourcePackManager by inject()

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        resourcePackManager.sendToPlayer(player)
    }
}
package cc.mewcraft.wakame.resource

import cc.mewcraft.wakame.user.toUser
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import me.lucko.helper.text3.mini
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ResourceListener(
    private val server: Server
) : Listener {
    @EventHandler
    fun onServerTick(event: ServerTickStartEvent) {
        for (player in server.onlinePlayers) {
            val user = player.toUser()
            user.resourceMap.add(ResourceTypeRegistry.MANA, 1)
            user.player.sendActionBar(user.resourceMap.current(ResourceTypeRegistry.MANA).toString().mini)
        }
    }
}
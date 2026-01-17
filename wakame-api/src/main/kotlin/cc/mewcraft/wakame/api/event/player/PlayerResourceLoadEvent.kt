package cc.mewcraft.wakame.api.event.player

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

class PlayerResourceLoadEvent(
    player: Player,
) : PlayerEvent(
    player, !Bukkit.isPrimaryThread()
) {

    override fun getHandlers(): HandlerList = HANDLER_LIST

    companion object {

        @JvmStatic
        private val HANDLER_LIST: HandlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLER_LIST
    }
}
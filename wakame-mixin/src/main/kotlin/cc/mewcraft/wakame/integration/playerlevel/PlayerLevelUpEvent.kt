package cc.mewcraft.wakame.integration.playerlevel

import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

class PlayerLevelUpEvent(
    player: Player,
    val skill: String,
    val level: Int,
) : PlayerEvent(player) {

    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {
        @JvmStatic
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }
}
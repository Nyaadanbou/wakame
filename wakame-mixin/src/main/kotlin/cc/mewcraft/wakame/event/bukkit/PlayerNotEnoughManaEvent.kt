package cc.mewcraft.wakame.event.bukkit

import cc.mewcraft.wakame.ability2.meta.AbilityMeta
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

class PlayerNotEnoughManaEvent(
    player: Player,
    val ability: AbilityMeta,
) : PlayerEvent(player) {
    override fun getHandlers(): HandlerList {
        return HANDLER_LIST
    }

    companion object {
        @JvmStatic
        val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLER_LIST
    }
}
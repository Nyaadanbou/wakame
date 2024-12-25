package cc.mewcraft.wakame.event

import cc.mewcraft.wakame.ecs.data.StatePhase
import cc.mewcraft.wakame.ability.Ability
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

class PlayerAbilityStateChangeEvent(
    player: Player,
    val ability: Ability,
    val oldPhase: StatePhase,
    val newPhase: StatePhase
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
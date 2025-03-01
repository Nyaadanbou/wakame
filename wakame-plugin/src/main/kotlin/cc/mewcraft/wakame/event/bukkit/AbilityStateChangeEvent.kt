package cc.mewcraft.wakame.event.bukkit

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ecs.data.StatePhase
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class AbilityStateChangeEvent(
    val ability: Ability,
    val oldPhase: StatePhase,
    val newPhase: StatePhase
) : Event() {

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
package cc.mewcraft.wakame.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class NekoLoadDataEvent : Event() {
    override fun getHandlers(): HandlerList = HANDLER_LIST

    companion object {
        @JvmStatic
        val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLER_LIST
    }
}
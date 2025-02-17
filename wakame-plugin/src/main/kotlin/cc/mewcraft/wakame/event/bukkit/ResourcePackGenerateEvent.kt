package cc.mewcraft.wakame.event.bukkit

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class ResourcePackGenerateEvent : Event(), Cancellable {
    private var cancel: Boolean = false

    override fun getHandlers(): HandlerList = HANDLER_LIST
    override fun isCancelled(): Boolean {
        return cancel
    }
    override fun setCancelled(cancel: Boolean) {
        this.cancel = cancel
    }

    companion object {
        @JvmStatic
        val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLER_LIST
    }
}
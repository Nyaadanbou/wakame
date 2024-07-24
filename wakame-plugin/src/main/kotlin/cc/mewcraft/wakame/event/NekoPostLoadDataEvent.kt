package cc.mewcraft.wakame.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * 当插件加载数据完成之后触发的事件.
 */
class NekoPostLoadDataEvent : Event(true) {
    override fun getHandlers(): HandlerList = HANDLER_LIST

    companion object {
        @JvmStatic
        val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLER_LIST
    }
}
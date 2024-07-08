package cc.mewcraft.wakame.eventbus

import cc.mewcraft.wakame.NEKO_PLUGIN
import com.github.shynixn.mccoroutine.bukkit.scope

object EventBusFactory {
    fun create(): EventBus {
        return EventBus(NEKO_PLUGIN.scope)
    }
}
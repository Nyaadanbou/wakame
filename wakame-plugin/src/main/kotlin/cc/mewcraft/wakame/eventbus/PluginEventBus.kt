package cc.mewcraft.wakame.eventbus

import cc.mewcraft.wakame.NEKO
import cc.mewcraft.wakame.SharedConstants
import com.github.shynixn.mccoroutine.bukkit.scope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus

internal object PluginEventBus {
    private val eventBus: EventBus =
        if (SharedConstants.isRunningInIde) {
            EventBus(CoroutineScope(Dispatchers.Unconfined) + SupervisorJob())
        } else {
            EventBus(NEKO.scope)
        }

    /**
     * 获取插件的 [EventBus].
     */
    fun get(): EventBus = eventBus
}
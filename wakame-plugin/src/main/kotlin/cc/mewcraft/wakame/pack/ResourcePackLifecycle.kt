package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.event.NekoCommandReloadEvent
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.util.runTask
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.koin.core.component.KoinComponent

internal object ResourcePackLifecycle : Initializable, KoinComponent {
    private fun initialize() {
        val service = ResourcePackServiceProvider.get()
        service.start()
    }

    override fun onPostWorld() {
        runTask { initialize() }
    }
}

/**
 * 负责在插件重载时, 重新配置资源包相关的系统.
 */
internal class ResourcePackLifecycleListener : Listener {
    @EventHandler
    fun onCommandReload(e: NekoCommandReloadEvent) {
        // 重新配置 资源包分发系统
        val service = ResourcePackServiceProvider.loadAndSet()
        service.start()

        // 重新配置 资源包发布系统
        val publisher = ResourcePackPublisherProvider.loadAndSet()
        publisher.cleanup()
    }
}
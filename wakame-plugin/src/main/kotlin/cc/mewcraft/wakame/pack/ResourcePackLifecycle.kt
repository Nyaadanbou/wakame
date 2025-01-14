package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.event.NekoCommandReloadEvent
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.util.runTask
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.koin.core.component.KoinComponent

@Init(
    stage = InitStage.POST_WORLD,
)
internal object ResourcePackLifecycle : KoinComponent {
    @InitFun
    private fun init() {
        runTask { ResourcePackServiceProvider.get().start() }
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
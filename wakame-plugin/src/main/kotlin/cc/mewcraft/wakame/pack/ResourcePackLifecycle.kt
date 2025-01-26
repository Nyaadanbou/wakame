package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.event
import cc.mewcraft.wakame.util.runTask
import org.bukkit.event.player.PlayerJoinEvent

@Init(
    stage = InitStage.POST_WORLD,
)
@Reload
internal object ResourcePackLifecycle {

    @InitFun
    fun init() {
        runTask { ResourcePackServiceProvider.get().start() }

        // 在玩家加入服务器时发送资源包.
        event<PlayerJoinEvent> { event ->
            val player = event.player
            val service = ResourcePackServiceProvider.get()

            service.sendPack(player)
        }
    }

    @ReloadFun
    fun reload() {
        // 重新配置 资源包分发系统
        val service = ResourcePackServiceProvider.loadAndSet()
        service.start()

        // 重新配置 资源包发布系统
        val publisher = ResourcePackPublisherProvider.loadAndSet()
        publisher.cleanup()
    }

}

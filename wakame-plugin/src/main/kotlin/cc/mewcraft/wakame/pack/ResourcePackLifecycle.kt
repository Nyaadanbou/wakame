package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.event
import cc.mewcraft.wakame.util.runTask
import org.bukkit.event.player.PlayerJoinEvent

@Init(InitStage.POST_WORLD)
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

    fun reload() {
        // 重新配置 资源包分发系统
        val service = ResourcePackServiceProvider.set()
        service.start()

        // 重新配置 资源包发布系统
        val publisher = ResourcePackPublisherProvider.loadAndSet()
        publisher.cleanup()
    }

}

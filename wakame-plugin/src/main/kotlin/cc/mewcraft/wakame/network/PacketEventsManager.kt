package cc.mewcraft.wakame.network

import cc.mewcraft.wakame.Koish
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.PacketEventsAPI
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder

@Init(
    stage = InitStage.POST_WORLD,
)
internal object PacketEventsManager {
    private val api: PacketEventsAPI<*>

    init {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(Koish))
        api = PacketEvents.getAPI()
        api.settings.apply {
            reEncodeByDefault(false)
            checkForUpdates(false)
        }
        api.load()
    }

    @InitFun
    private fun init() {
        api.eventManager.registerListeners(
            ItemEntityRenderer,
            ItemStackRenderer
        )
        api.init() // init 必须在 registerListener 之后调用
    }

    @DisableFun
    private fun close() {
        api.terminate()
    }
}
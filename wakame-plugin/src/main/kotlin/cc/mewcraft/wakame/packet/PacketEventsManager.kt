package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.initializer2.DisableFun
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.PacketEventsAPI
import com.github.retrooper.packetevents.event.PacketListenerCommon
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder

@Init(
    stage = InitStage.PRE_WORLD
)
internal object PacketEventsManager {
    private val api: PacketEventsAPI<*>

    init {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(Injector.get()))
        api = PacketEvents.getAPI()
        api.settings.apply {
            reEncodeByDefault(false)
            checkForUpdates(true)
        }
        api.load()
    }

    @InitFun
    private fun init() {
        Injector.getKoin()
            .getAll<PacketListenerCommon>()
            .forEach(api.eventManager::registerListener)
        api.init() // init 必须在 registerListener 之后调用
    }

    @DisableFun
    private fun close() {
        api.terminate()
    }
}
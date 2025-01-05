package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.initializer2.DisableFun
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.PacketEventsAPI
import com.github.retrooper.packetevents.event.PacketListenerCommon
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

@Init(
    stage = InitStage.PRE_WORLD
)
internal object PacketEventsManager : KoinComponent {
    private val api: PacketEventsAPI<*>

    init {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(get()))
        api = PacketEvents.getAPI()
        // Are all listeners read only?
        api.settings
            .reEncodeByDefault(false)
            .checkForUpdates(true)
        api.load()
    }

    @InitFun
    fun onPreWorld() {
        getKoin().getAll<PacketListenerCommon>()
            .forEach { api.eventManager.registerListener(it) }
        api.init() // init 必须在 registerListener 之后调用
    }

    @DisableFun
    fun close() {
        api.terminate()
    }
}
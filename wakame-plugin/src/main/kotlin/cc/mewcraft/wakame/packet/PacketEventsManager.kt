package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.initializer.Initializable
import com.github.retrooper.packetevents.PacketEventsAPI
import com.github.retrooper.packetevents.event.PacketListenerCommon
import org.koin.core.component.KoinComponent

internal class PacketEventsManager(
    private val api: PacketEventsAPI<*>,
) : Initializable, KoinComponent {

    init {
        // Are all listeners read only?
        api.settings
            .reEncodeByDefault(false)
            .checkForUpdates(true)
        api.load()
    }

    override fun onPreWorld() {
        getKoin().getAll<PacketListenerCommon>()
            .forEach { api.eventManager.registerListener(it) }
        api.init() // init 必须在 registerListener 之后调用
    }

    override fun close() {
        api.terminate()
    }
}
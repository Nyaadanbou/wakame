package cc.mewcraft.wakame.packet

import cc.mewcraft.wakame.initializer.Initializable
import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.PacketEventsAPI
import com.github.retrooper.packetevents.event.PacketListenerCommon
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun packetModule() = module {

    single<PacketEventsAPI<*>> {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(get()))
        PacketEvents.getAPI()
    }

    singleOf(::PacketEventsManager) bind Initializable::class

    singleOf(::FOVPacketHandler) bind PacketListenerCommon::class
    singleOf(::GlowingItemPacketHandler) bind PacketListenerCommon::class
    singleOf(::PacketNekoStackRenderListener) bind PacketListenerCommon::class
}
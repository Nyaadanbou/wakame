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
    // TODO: 将 package 名改成 network

    single<PacketEventsAPI<*>> {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(get()))
        PacketEvents.getAPI()
    }

    singleOf(::PacketEventsManager) bind Initializable::class
    singleOf(::DamageDisplay)
    // singleOf(::FOVLimiter) bind PacketListenerCommon::class // 飞行模式下视角会鬼畜, 暂时移除
    singleOf(::ItemEntityRenderer) bind PacketListenerCommon::class
    singleOf(::ItemStackRenderer) bind PacketListenerCommon::class
}
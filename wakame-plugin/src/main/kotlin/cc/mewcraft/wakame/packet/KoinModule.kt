package cc.mewcraft.wakame.packet

import com.github.retrooper.packetevents.event.PacketListenerCommon
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun packetModule() = module {
    // TODO: 将 package 名改成 network

    singleOf(::ItemEntityRenderer) bind PacketListenerCommon::class
    singleOf(::ItemStackRenderer) bind PacketListenerCommon::class
}
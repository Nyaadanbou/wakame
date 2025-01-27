package cc.mewcraft.wakame.network

import com.github.retrooper.packetevents.event.PacketListenerCommon
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun packetModule() = module {
    singleOf(::ItemEntityRenderer) bind PacketListenerCommon::class
    singleOf(::ItemStackRenderer) bind PacketListenerCommon::class
}
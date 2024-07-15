package cc.mewcraft.wakame.tick

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun tickModule(): Module = module {
    singleOf(::BukkitTicker) bind Ticker::class
}
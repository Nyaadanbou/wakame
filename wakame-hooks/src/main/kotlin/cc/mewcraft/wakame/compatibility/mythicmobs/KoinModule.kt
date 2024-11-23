package cc.mewcraft.wakame.compatibility.mythicmobs

import org.bukkit.event.Listener
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun mythicMobsModule() = module {
    single { MythicMobsListener() } bind Listener::class
}
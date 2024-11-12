package cc.mewcraft.wakame.compatibility.mythicmobs

import org.bukkit.event.Listener
import org.koin.dsl.module

internal fun mythicMobsModule() = module {
    single<Listener> { MythicMobsListener() }
}
package cc.mewcraft.wakame.compatibility

import cc.mewcraft.wakame.compatibility.mythicmobs.MythicMobsListener
import org.bukkit.event.Listener
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun compatibilityModule(): Module = module {
    singleOf(::MythicMobsListener) bind Listener::class
}
package cc.mewcraft.wakame.skill.state

import org.bukkit.event.Listener
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun stateModule() = module {
    singleOf(::StateListener) bind Listener::class
}
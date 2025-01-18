package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.ability.state.display.PlayerStateDisplay
import cc.mewcraft.wakame.ability.state.display.StateDisplay
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

fun abilityModule(): Module = module {
    singleOf(::PlayerStateDisplay) bind StateDisplay::class

    singleOf(::AbilityEventHandler)
}
package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.registry.AbilityRegistry
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

fun abilityModule(): Module = module {

    single { AbilityRegistry } bind Initializable::class

}
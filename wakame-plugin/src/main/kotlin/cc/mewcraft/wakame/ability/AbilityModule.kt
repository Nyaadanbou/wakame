package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

fun abilityModule(): Module = module {

    single { AbilityFacadeRegistry } bind Initializable::class

}
package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.registry.AbilityRegistry
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun abilityModule(): Module = module {

    singleOf(::AbilityEventHandler)
    single { AbilityRegistry } bind Initializable::class

}
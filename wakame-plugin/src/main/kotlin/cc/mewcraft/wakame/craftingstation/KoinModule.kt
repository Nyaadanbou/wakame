package cc.mewcraft.wakame.craftingstation

import cc.mewcraft.wakame.craftingstation.recipe.StationRecipeRegistry
import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module


internal fun stationModule(): Module = module {
    single { StationRecipeRegistry } bind Initializable::class
    single { StationRegistry } bind Initializable::class
}
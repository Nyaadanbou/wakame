package cc.mewcraft.wakame.station

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.station.recipe.StationRecipeRegistry
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module


internal fun stationModule(): Module = module {
    single { StationRecipeRegistry } bind Initializable::class
    single { StationRegistry } bind Initializable::class
}
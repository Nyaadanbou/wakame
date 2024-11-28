package cc.mewcraft.wakame.craftingstation

import cc.mewcraft.wakame.craftingstation.recipe.CraftingStationRecipeRegistry
import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module


internal fun stationModule(): Module = module {
    single { CraftingStationRecipeRegistry } bind Initializable::class
    single { CraftingStationRegistry } bind Initializable::class
}
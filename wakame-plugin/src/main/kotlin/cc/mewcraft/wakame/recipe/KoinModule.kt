package cc.mewcraft.wakame.recipe

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module


internal fun recipeModule(): Module = module {
    single { VanillaRecipeRegistry } bind Initializable::class
}
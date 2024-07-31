package cc.mewcraft.wakame.craft.recipe

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module


internal fun recipeModule(): Module = module {
    single { RecipeRegistry } bind Initializable::class
}
package cc.mewcraft.wakame.hook.impl.breweryx

import cc.mewcraft.wakame.brew.BarrelWoodType
import cc.mewcraft.wakame.brew.BrewRecipe
import cc.mewcraft.wakame.brew.BrewRecipeAdapter
import com.dre.brewery.recipe.BRecipe


/**
 * 使用 BreweryX 实现的 [BrewRecipeAdapter].
 */
object TheBrewRecipeAdapter : BrewRecipeAdapter<BRecipe> {

    override fun adapt(recipe: BRecipe): BrewRecipe {
        return BrewRecipe(
            id = recipe.id,
            name = recipe.recipeName,
            difficulty = recipe.difficulty,
            cookingTime = recipe.cookingTime,
            distillTime = recipe.distillTime,
            distillRuns = recipe.distillruns.toInt(),
            age = recipe.age,
            woodType = BarrelWoodType.REGISTRY.get(recipe.wood.index) ?: BarrelWoodType.NONE,
            lore = recipe.lore?.map { it.second() } ?: emptyList(),
            ingredients = recipe.ingredients.associate { it.configId to it.amount },
            potionColor = recipe.color.color,
        )
    }
}

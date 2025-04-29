package cc.mewcraft.wakame.hook.impl.breweryx

import cc.mewcraft.wakame.brewery.BarrelWoodType
import cc.mewcraft.wakame.brewery.BrewRecipe
import cc.mewcraft.wakame.brewery.BrewRecipeAdapter
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
            barrelType = BarrelWoodType.REGISTRY.get(recipe.wood.index) ?: BarrelWoodType.NONE,
            lore = recipe.lore?.map { tuple -> tuple.second() } ?: emptyList(),
            ingredients = recipe.ingredients.associate { recipeItem ->
                val configString = recipeItem.toConfigString() // something like "dirt/1", "stone/2"
                val (item, amount) = configString.split("/")
                Pair(item, amount.toInt())
            },
            potionColor = recipe.color.color,
        )
    }
}

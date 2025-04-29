package cc.mewcraft.wakame.hook.impl.breweryx

import cc.mewcraft.wakame.brewery.BrewRecipe
import cc.mewcraft.wakame.brewery.BrewRecipeRenderer
import cc.mewcraft.wakame.config.ConfigAccess
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.item2.display.SlotDisplayLoreData
import cc.mewcraft.wakame.util.text.mini
import net.kyori.adventure.text.Component


/**
 * 一般的 [BrewRecipeRenderer] 实现.
 */
object TheBrewRecipeRenderer : BrewRecipeRenderer {
    override fun render(recipe: BrewRecipe): List<Component> {
        // TODO #383: 渲染格式数据驱动化
        return listOf(
            Component.text("Name: ${recipe.name}"),
            Component.text("Difficulty: ${recipe.difficulty}"),
            Component.text("Cooking Time: ${recipe.cookingTime}"),
            Component.text("Distill Runs: ${recipe.distillRuns}"),
            Component.text("Distill Time: ${recipe.distillTime}"),
            Component.text("Age: ${recipe.age}"),
            Component.text("Wood Type: ${recipe.woodType}"),
            Component.text("Lore: ${recipe.lore.joinToString(", ")}"),
            Component.text("Ingredients: ${recipe.ingredients.entries.joinToString(", ") { "${it.key}: ${it.value}" }}"),
        )
    }
}
package cc.mewcraft.wakame.hook.impl.breweryx

import cc.mewcraft.wakame.brewery.BrewRecipe
import cc.mewcraft.wakame.brewery.BrewRecipeRenderer
import cc.mewcraft.wakame.config.ConfigAccess
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.item.SlotDisplayLoreData
import cc.mewcraft.wakame.util.adventure.legacy
import net.kyori.adventure.text.Component

private val CONFIG = ConfigAccess.INSTANCE["brewery/config"]

/**
 * 一般的 [BrewRecipeRenderer] 实现.
 */
object TheBrewRecipeRenderer : BrewRecipeRenderer {

    private val loreFormat: SlotDisplayLoreData by CONFIG.entry("lore_format")

    override fun render(recipe: BrewRecipe): List<Component> {
        return loreFormat.resolve {
            standard {
                component("name", Component.text(recipe.name))
                component("difficulty", Component.text(recipe.difficulty))
                component("cooking_time", Component.text(recipe.cookingTime))
                component("distill_runs", Component.text(recipe.distillRuns))
                component("distill_time", Component.text(recipe.distillTime))
                component("age", Component.text(recipe.age))
                component("barrel_type", Component.text(recipe.barrelType.formattedName))
            }
            folded("lore", recipe.lore.legacy)
            folded("ingredient", recipe.ingredients.map { (item, amount) -> Component.text("$item/$amount") })
        }
    }
}
package cc.mewcraft.wakame.craft.recipe

import cc.mewcraft.wakame.util.toNamespacedKey
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.inventory.RecipeChoice as BukkitRecipeChoice

/**
 * 工作台无序合成.
 */
class ShapelessRecipe(
    override val key: Key,
    override val recipeResult: RecipeResult,
    private val ingredients: List<RecipeChoice>
) : Recipe {
    override fun addBukkitRecipe() {
        val shapelessRecipe = ShapelessRecipe(key.toNamespacedKey, recipeResult.toBukkitItemStack())
        ingredients.forEach {
            shapelessRecipe.addIngredient(BukkitRecipeChoice.ExactChoice(it.toBukkitItemStacks()))
        }
        Bukkit.addRecipe(shapelessRecipe)
    }
}
package cc.mewcraft.wakame.craft.recipe

import cc.mewcraft.wakame.util.toNamespacedKey
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type
import org.bukkit.inventory.RecipeChoice as BukkitRecipeChoice
import org.bukkit.inventory.ShapedRecipe as BukkitShapedRecipe

/**
 * 工作台有序合成.
 */
class ShapedRecipe(
    override val key: Key,
    override val recipeResult: RecipeResult,
    private val rows: Array<String>,
    private val ingredients: Map<Char, RecipeChoice>
) : Recipe {
    override fun addBukkitRecipe() {
        val shapedRecipe = BukkitShapedRecipe(key.toNamespacedKey, recipeResult.toBukkitItemStack())
        shapedRecipe.shape(*rows)
        ingredients.forEach {
            shapedRecipe.setIngredient(it.key, BukkitRecipeChoice.ExactChoice(it.value.toBukkitItemStacks()))
        }
        Bukkit.addRecipe(shapedRecipe)
    }
}

internal object ShapedRecipeSerializer : TypeSerializer<ShapedRecipe> {
    override fun deserialize(type: Type, node: ConfigurationNode): ShapedRecipe {
        TODO("Not yet implemented")
    }

    override fun serialize(type: Type, obj: ShapedRecipe?, node: ConfigurationNode) {
        throw UnsupportedOperationException()
    }

}

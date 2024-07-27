package cc.mewcraft.wakame.craft.recipe

import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.util.toNamespacedKey
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack

/**
 * 合成配方.
 * 是对Bukkit的合成配方的包装
 */
sealed interface Recipe : Keyed {
    val recipeResult: RecipeResult

    fun addBukkitRecipe()

    fun removeBukkitRecipe() {
        Bukkit.removeRecipe(key.toNamespacedKey, false)
    }
}

/**
 * 合成配方的输入.
 * 表现为合成输入gui中一格的物品.
 */
sealed interface RecipeChoice {
    fun toBukkitItemStacks(): List<ItemStack>
}

/**
 * 单物品输入.
 */
class SingleRecipeChoice(
    val choice: Key
) : RecipeChoice {
    override fun toBukkitItemStacks(): List<ItemStack> {
        TODO("Not yet implemented")
    }

}

/**
 * 多物品输入.
 */
class MultiRecipeChoice(
    val choices: Key
) : RecipeChoice {
    override fun toBukkitItemStacks(): List<ItemStack> {
        TODO("Not yet implemented")
    }
}

/**
 * 合成配方的输出.
 * 表现为合成输出gui中一格的物品.
 */
interface RecipeResult {
    fun toBukkitItemStack(): ItemStack
}

/**
 * 单物品输出.
 */
class SingleRecipeResult(
    val result: Key
) : RecipeResult {
    override fun toBukkitItemStack(): ItemStack {
        TODO("Not yet implemented")
    }
}
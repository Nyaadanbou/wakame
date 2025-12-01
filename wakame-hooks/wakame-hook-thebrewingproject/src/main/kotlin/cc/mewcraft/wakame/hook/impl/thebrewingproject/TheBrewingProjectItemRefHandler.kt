package cc.mewcraft.wakame.hook.impl.thebrewingproject

import cc.mewcraft.wakame.item.ItemRefHandler
import cc.mewcraft.wakame.util.Identifier
import dev.jsinco.brewery.api.brew.Brew
import dev.jsinco.brewery.api.brew.BrewQuality
import dev.jsinco.brewery.api.recipe.Recipe
import dev.jsinco.brewery.bukkit.TheBrewingProject
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi
import dev.jsinco.brewery.bukkit.brew.BrewAdapter
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.jvm.optionals.getOrNull

/**
 * 实现了 [ItemRefHandler] 以便让 Koish 可以识别 TheBrewingProject 的物品.
 *
 * ### 物品格式
 * - "thebrewingproject:example" -> id 为 example
 */
object TheBrewingProjectItemRefHandler : ItemRefHandler<Recipe<ItemStack>> {

    const val NAMESPACE = "brew"

    override val systemName: String = "TheBrewingProject"

    private val tbpApi: TheBrewingProjectApi
        get() = TheBrewingProject.getInstance()

    override fun accepts(id: Identifier): Boolean {
        if (id.namespace() != NAMESPACE) return false
        val recipeId: String = id.value().extractRecipeId()
        val recipe: Recipe<ItemStack>? = tbpApi.recipeRegistry.getRecipe(recipeId).getOrNull()
        return recipe != null
    }

    override fun getId(stack: ItemStack): Identifier? {
        val brew: Brew = BrewAdapter.fromItem(stack).getOrNull() ?: return null
        val closestRecipe: Recipe<ItemStack> = brew.closestRecipe(tbpApi.recipeRegistry).getOrNull() ?: return null
        val brewQuality: BrewQuality = brew.quality(closestRecipe).getOrNull() ?: return null
        return Key.key(NAMESPACE, "${closestRecipe.recipeName}/${brewQuality.ordinal}")
    }

    override fun getName(id: Identifier): Component? {
        if (id.namespace() != NAMESPACE) return null
        val recipeId: String = id.value().extractRecipeId()
        val recipe: Recipe<ItemStack> = tbpApi.recipeRegistry.getRecipe(recipeId).getOrNull() ?: return null
        return Component.text(recipe.recipeName)
    }

    override fun getInternalType(id: Identifier): Recipe<ItemStack>? {
        // Brewery API 没办法从 id 拿到一个具有特定 quality 的 BRecipe,
        // 因为 Recipe 本身就包含了一个 recipe 可能出现的所有 quality.
        // 而不同的 quality 在我们的定义下属于不同的 ItemRef,
        // 所以干脆这里返回 null 来表示我们不使用这个函数.

        return null
    }

    override fun createItemStack(id: Identifier, amount: Int, player: Player?): ItemStack? {
        if (id.namespace() != NAMESPACE) return null
        val value: String = id.value()
        val recipeId: String = value.extractRecipeId()
        val recipe: Recipe<ItemStack> = tbpApi.recipeRegistry.getRecipe(recipeId).getOrNull() ?: return null
        val brewQuality: BrewQuality = recipeId.extractIntQuality()?.toBrewQuality() ?: return null
        val brewItem: ItemStack = recipe.getRecipeResult(brewQuality).newLorelessItem()
        return brewItem
    }

    private fun String.extractRecipeId(): String {
        return substringBeforeLast('/')
    }

    private fun String.extractIntQuality(): Int? {
        return substringAfterLast('/').toIntOrNull()
    }

    private fun Int.toBrewQuality(): BrewQuality? {
        return when (this) {
            0 -> BrewQuality.BAD
            1 -> BrewQuality.GOOD
            2 -> BrewQuality.EXCELLENT
            else -> null
        }
    }
}
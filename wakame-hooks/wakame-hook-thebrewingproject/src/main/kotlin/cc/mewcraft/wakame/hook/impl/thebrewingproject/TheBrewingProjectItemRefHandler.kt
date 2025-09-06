package cc.mewcraft.wakame.hook.impl.thebrewingproject

import cc.mewcraft.wakame.item2.ItemRefHandler
import cc.mewcraft.wakame.util.Identifier
import dev.jsinco.brewery.brew.Brew
import dev.jsinco.brewery.brew.BrewImpl
import dev.jsinco.brewery.bukkit.TheBrewingProject
import dev.jsinco.brewery.bukkit.brew.BrewAdapter
import dev.jsinco.brewery.recipe.Recipe
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

    override fun accepts(id: Identifier): Boolean {
        if (id.namespace() != NAMESPACE) return false
        val recipeId = id.value().extractRecipeId()
        val recipeRegistry = TheBrewingProject.getInstance().recipeRegistry
        val recipe = recipeRegistry.getRecipe(recipeId)
        return recipe != null
    }

    override fun getId(stack: ItemStack): Identifier? {
        val brew = BrewAdapter.fromItem(stack).getOrNull() ?: return null
        val recipeRegistry = TheBrewingProject.getInstance().recipeRegistry
        val closestRecipe = brew.closestRecipe(recipeRegistry).getOrNull() ?: return null
        val brewQuality = brew.quality(closestRecipe).getOrNull() ?: return null
        return Key.key(NAMESPACE, "${closestRecipe.recipeName}/${brewQuality.ordinal}")
    }

    override fun getName(id: Identifier): Component? {
        if (id.namespace() != NAMESPACE) return null
        val recipeId = id.value().extractRecipeId()
        val recipeRegistry = TheBrewingProject.getInstance().recipeRegistry
        val recipe = recipeRegistry.getRecipe(recipeId).getOrNull() ?: return null
        return Component.text(recipe.recipeName)
    }

    override fun getInternalType(id: Identifier): Recipe<ItemStack>? {
        // Brewery API 没办法从 id 拿到一个具有特定 quality 的 BRecipe,
        // 因为 BRecipe 本身就包含了一个 recipe 可能出现的所有 quality.
        // 而不同的 quality 在我们的定义下是属于不同的 ItemRef 的.
        // 所以干脆这里返回 null 来表示我们不使用这个函数.

        return null
    }

    override fun createItemStack(id: Identifier, amount: Int, player: Player?): ItemStack? {
        if (id.namespace() != NAMESPACE) return null
        val value = id.value()
        val recipeId = value.extractRecipeId()
        val recipeRegistry = TheBrewingProject.getInstance().recipeRegistry
        val recipe = recipeRegistry.getRecipe(recipeId).getOrNull() ?: return null
        val brew = BrewImpl(recipe.steps)
        val brewItem = BrewAdapter.toItem(brew, Brew.State.Other())
        return brewItem
    }

    private fun String.extractRecipeId(): String {
        return substringBeforeLast('/')
    }

    // TODO 之后直接问 Thorinwasher 怎么从 id 去生成特定品质的酒
    // private fun String.extractIntQuality(): Int? {
    //     return substringAfterLast('/').toIntOrNull()
    // }
    //
    // private fun Int.toBrewQuality(): BrewQuality? {
    //     return when (this) {
    //         0 -> BrewQuality.BAD
    //         1 -> BrewQuality.GOOD
    //         2 -> BrewQuality.EXCELLENT
    //         else -> null
    //     }
    // }
}
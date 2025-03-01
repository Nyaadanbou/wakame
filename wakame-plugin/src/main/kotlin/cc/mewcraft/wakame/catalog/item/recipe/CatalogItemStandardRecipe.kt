package cc.mewcraft.wakame.catalog.item.recipe

import cc.mewcraft.wakame.core.ItemX
import cc.mewcraft.wakame.core.ItemXFactoryRegistry
import cc.mewcraft.wakame.core.ItemXNoOp
import cc.mewcraft.wakame.core.ItemXVanilla
import org.bukkit.inventory.*
import org.bukkit.inventory.Recipe as BukkitRecipe

/**
 * 标准的 [CatalogRecipe], 代表狭义上的 [物品合成配方](https://minecraft.wiki/w/Recipe).
 */
abstract class CatalogStandardRecipe(
    private val recipe: BukkitRecipe,
) : CatalogRecipe {

    private val outputs: Set<ItemX> = setOfNotNull(ItemXFactoryRegistry[recipe.result])
    override fun getLookupOutputs(): Set<ItemX> = outputs
    fun <T : BukkitRecipe> recipe(): T = recipe as T

}

/**
 * 抽象的烧制配方.
 * 意在和原版一样减少一些重复代码.
 */
abstract class CatalogCookingRecipe(
    recipe: CookingRecipe<*>,
) : CatalogStandardRecipe(recipe) {
    val cookingTime = recipe.cookingTime
    val experience = recipe.experience

    // 对应原版该类型配方对 RecipeChoice 存储格式
    // 意在缓存各个 RecipeChoice 转化为 List<ItemX> 的结果
    val inputItems: List<ItemX> = recipe.inputChoice.toItems()

    // 缓存配方的输出转化为 ItemX 的结果
    // TODO 通过改进 ItemX 使得这里不用强转非空
    val outputItems: ItemX = ItemXFactoryRegistry[recipe.result]!!

    override val sortId: String = recipe.key.value()
    override fun getLookupInputs(): Set<ItemX> = inputItems.toSet()
}


class CatalogBlastingRecipe(
    recipe: BlastingRecipe,
) : CatalogCookingRecipe(recipe) {
    override val type = CatalogRecipeType.BLASTING_RECIPE
}

class CatalogCampfireRecipe(
    recipe: CampfireRecipe,
) : CatalogCookingRecipe(recipe) {
    override val type = CatalogRecipeType.CAMPFIRE_RECIPE
}

class CatalogFurnaceRecipe(
    recipe: FurnaceRecipe,
) : CatalogCookingRecipe(recipe) {
    override val type = CatalogRecipeType.FURNACE_RECIPE
}

class CatalogShapedRecipe(
    recipe: ShapedRecipe,
) : CatalogStandardRecipe(recipe) {
    val shape: Array<out String> = recipe.shape
    val inputItems: Map<Char, List<ItemX>> = recipe.choiceMap.mapValues { it.value.toItems() }
    val outputItem: ItemX = ItemXFactoryRegistry[recipe.result]!!

    override val type = CatalogRecipeType.SHAPED_RECIPE
    override val sortId: String = recipe.key.value()
    override fun getLookupInputs(): Set<ItemX> = inputItems.values.flatten().toSet()
}

class CatalogShapelessRecipe(
    recipe: ShapelessRecipe,
) : CatalogStandardRecipe(recipe) {
    val inputItems: List<List<ItemX>> = recipe.choiceList.map { it.toItems() }
    val outputItems: ItemX = ItemXFactoryRegistry[recipe.result]!!

    override val type = CatalogRecipeType.SHAPELESS_RECIPE
    override val sortId: String = recipe.key.value()
    override fun getLookupInputs(): Set<ItemX> = inputItems.flatten().toSet()
}

class CatalogSmithingTransformRecipe(
    recipe: SmithingTransformRecipe,
) : CatalogStandardRecipe(recipe) {
    val baseItems: List<ItemX> = recipe.base.toItems()
    val templateItems: List<ItemX> = recipe.template.toItems()
    val additionItems: List<ItemX> = recipe.addition.toItems()
    val outputItemX: ItemX = ItemXFactoryRegistry[recipe.result]!!

    override val type = CatalogRecipeType.SMITHING_TRANSFORM_RECIPE
    override val sortId: String = recipe.key.value()
    override fun getLookupInputs(): Set<ItemX> = (baseItems + templateItems + additionItems).toSet()
}

class CatalogSmithingTrimRecipe(
    recipe: SmithingTrimRecipe,
) : CatalogStandardRecipe(recipe) {
    val baseItems: List<ItemX> = recipe.base.toItems()
    val templateItems: List<ItemX> = recipe.template.toItems()
    val additionItems: List<ItemX> = recipe.addition.toItems()

    override val type = CatalogRecipeType.SMITHING_TRIM_RECIPE
    override val sortId: String = recipe.key.value()
    override fun getLookupInputs(): Set<ItemX> = (baseItems + templateItems + additionItems).toSet()

    // 锻造台纹饰配方没有输出, 返回一个占位物品作为节点
    override fun getLookupOutputs(): Set<ItemX> = setOf(ItemXNoOp)
}

class CatalogSmokingRecipe(
    recipe: SmokingRecipe,
) : CatalogCookingRecipe(recipe) {
    override val type = CatalogRecipeType.SMOKING_RECIPE
    override val sortId: String = recipe.key.value()
}

class CatalogStonecuttingRecipe(
    recipe: StonecuttingRecipe,
) : CatalogStandardRecipe(recipe) {
    val inputItems: List<ItemX> = recipe.inputChoice.toItems()
    val outputItem: ItemX = ItemXFactoryRegistry[recipe.result]!!

    override val type = CatalogRecipeType.STONECUTTING_RECIPE
    override val sortId: String = recipe.key.value()
    override fun getLookupInputs(): Set<ItemX> = inputItems.toSet()
}

/**
 * 方便函数.
 */
private fun RecipeChoice?.toItems(): List<ItemX> {
    return when (this) {
        is RecipeChoice.ExactChoice -> choices.mapNotNull { ItemXFactoryRegistry[it] }
        is RecipeChoice.MaterialChoice -> choices.map { ItemXVanilla(it.name.lowercase()) }
        else -> emptyList()
    }
}
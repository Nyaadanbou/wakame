package cc.mewcraft.wakame.catalog.item.recipe

import cc.mewcraft.wakame.item2.ItemRef
import org.bukkit.inventory.BlastingRecipe
import org.bukkit.inventory.CampfireRecipe
import org.bukkit.inventory.CookingRecipe
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.inventory.SmithingTransformRecipe
import org.bukkit.inventory.SmithingTrimRecipe
import org.bukkit.inventory.SmokingRecipe
import org.bukkit.inventory.StonecuttingRecipe
import org.bukkit.inventory.Recipe as BukkitRecipe

/**
 * 标准的 [CatalogRecipe], 代表狭义上的 [物品合成配方](https://minecraft.wiki/w/Recipe).
 */
abstract class CatalogStandardRecipe(
    private val recipe: BukkitRecipe,
) : CatalogRecipe {

    private val outputs: Set<ItemRef> = setOfNotNull(ItemRef.checkedItemRef(recipe.result))
    override fun getLookupOutputs(): Set<ItemRef> = outputs
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
    // 意在缓存各个 RecipeChoice 转化为 List<ItemRef> 的结果
    val inputItems: List<ItemRef> = recipe.inputChoice.toItems()

    // 缓存配方的输出转化为 ItemRef 的结果
    val outputItems: ItemRef = ItemRef.checkedItemRef(recipe.result)

    override val sortId: String = recipe.key.value()
    override fun getLookupInputs(): Set<ItemRef> = inputItems.toSet()
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
    val inputItems: Map<Char, List<ItemRef>> = recipe.choiceMap.mapValues { it.value.toItems() }
    val outputItem: ItemRef = ItemRef.checkedItemRef(recipe.result)

    override val type = CatalogRecipeType.SHAPED_RECIPE
    override val sortId: String = recipe.key.value()
    override fun getLookupInputs(): Set<ItemRef> = inputItems.values.flatten().toSet()
}

class CatalogShapelessRecipe(
    recipe: ShapelessRecipe,
) : CatalogStandardRecipe(recipe) {
    val inputItems: List<List<ItemRef>> = recipe.choiceList.map { it.toItems() }
    val outputItems: ItemRef = ItemRef.checkedItemRef(recipe.result)

    override val type = CatalogRecipeType.SHAPELESS_RECIPE
    override val sortId: String = recipe.key.value()
    override fun getLookupInputs(): Set<ItemRef> = inputItems.flatten().toSet()
}

class CatalogSmithingTransformRecipe(
    recipe: SmithingTransformRecipe,
) : CatalogStandardRecipe(recipe) {
    val baseItems: List<ItemRef> = recipe.base.toItems()
    val templateItems: List<ItemRef> = recipe.template.toItems()
    val additionItems: List<ItemRef> = recipe.addition.toItems()
    val outputItemRef: ItemRef = ItemRef.checkedItemRef(recipe.result)

    override val type = CatalogRecipeType.SMITHING_TRANSFORM_RECIPE
    override val sortId: String = recipe.key.value()
    override fun getLookupInputs(): Set<ItemRef> = (baseItems + templateItems + additionItems).toSet()
}

class CatalogSmithingTrimRecipe(
    recipe: SmithingTrimRecipe,
) : CatalogStandardRecipe(recipe) {
    val baseItems: List<ItemRef> = recipe.base.toItems()
    val templateItems: List<ItemRef> = recipe.template.toItems()
    val additionItems: List<ItemRef> = recipe.addition.toItems()

    override val type = CatalogRecipeType.SMITHING_TRIM_RECIPE
    override val sortId: String = recipe.key.value()
    override fun getLookupInputs(): Set<ItemRef> = (baseItems + templateItems + additionItems).toSet()

    // 锻造台纹饰配方没有输出, 返回一个占位物品作为节点
    override fun getLookupOutputs(): Set<ItemRef> = setOf(ItemRef.noop())
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
    val inputItems: List<ItemRef> = recipe.inputChoice.toItems()
    val outputItem: ItemRef = ItemRef.checkedItemRef(recipe.result)

    override val type = CatalogRecipeType.STONECUTTING_RECIPE
    override val sortId: String = recipe.key.value()
    override fun getLookupInputs(): Set<ItemRef> = inputItems.toSet()
}

/**
 * 方便函数.
 */
private fun RecipeChoice?.toItems(): List<ItemRef> {
    return when (this) {
        is RecipeChoice.ExactChoice -> choices.mapNotNull { ItemRef.checkedItemRef(it) }
        is RecipeChoice.MaterialChoice -> choices.map { ItemRef.uncheckedItemRef(it.key) }
        else -> emptyList()
    }
}
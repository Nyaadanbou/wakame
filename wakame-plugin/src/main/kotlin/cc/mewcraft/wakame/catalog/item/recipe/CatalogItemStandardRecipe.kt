package cc.mewcraft.wakame.catalog.item.recipe

import cc.mewcraft.wakame.item.ItemRef
import cc.mewcraft.wakame.util.KoishKey
import org.bukkit.Keyed
import org.bukkit.inventory.*
import org.bukkit.inventory.Recipe as BukkitRecipe

/**
 * 标准的 [CatalogItemRecipe], 代表狭义上的 [物品合成配方](https://minecraft.wiki/w/Recipe).
 */
abstract class CatalogItemStandardRecipe(
    private val recipe: BukkitRecipe,
) : CatalogItemRecipe {
    /**
     * 该配方在注册表中的唯一标识.
     */
    val recipeId: KoishKey = (recipe as Keyed).key()

    private val outputs: Set<ItemRef> = setOfNotNull(ItemRef.create(recipe.result))

    override fun getLookupOutputs(): Set<ItemRef> = outputs
    @Suppress("UNCHECKED_CAST")
    fun <T : BukkitRecipe> recipe(): T = recipe as T
}

/**
 * 抽象的烧制配方.
 * 意在和原版一样减少一些重复代码.
 */
abstract class CatalogItemCookingRecipe(
    recipe: CookingRecipe<*>,
) : CatalogItemStandardRecipe(recipe) {
    val cookingTime = recipe.cookingTime
    val experience = recipe.experience

    // 对应原版该类型配方对 RecipeChoice 存储格式
    // 意在缓存各个 RecipeChoice 转化为 List<ItemRef> 的结果
    val inputItems: List<ItemRef> = recipe.inputChoice.toItems()

    // 缓存配方的输出转化为 ItemRef 的结果
    val outputItems: ItemRef = ItemRef.create(recipe.result)

    override val sortId: String = recipe.key.value()
    override fun getLookupInputs(): Set<ItemRef> = inputItems.toSet()
}


class CatalogItemBlastingRecipe(
    recipe: BlastingRecipe,
) : CatalogItemCookingRecipe(recipe) {
    override val type = CatalogItemRecipeType.BLASTING_RECIPE
}

class CatalogItemCampfireRecipe(
    recipe: CampfireRecipe,
) : CatalogItemCookingRecipe(recipe) {
    override val type = CatalogItemRecipeType.CAMPFIRE_RECIPE
}

class CatalogItemFurnaceRecipe(
    recipe: FurnaceRecipe,
) : CatalogItemCookingRecipe(recipe) {
    override val type = CatalogItemRecipeType.FURNACE_RECIPE
}

class CatalogItemShapedRecipe(
    recipe: ShapedRecipe,
) : CatalogItemStandardRecipe(recipe) {
    val shape: Array<out String> = recipe.shape
    val inputItems: Map<Char, List<ItemRef>> = recipe.choiceMap.mapValues { it.value.toItems() }
    val outputItem: ItemRef = ItemRef.create(recipe.result)

    override val type = CatalogItemRecipeType.SHAPED_RECIPE
    override val sortId: String = recipe.key.value()
    override fun getLookupInputs(): Set<ItemRef> = inputItems.values.flatten().toSet()
}

class CatalogItemShapelessRecipe(
    recipe: ShapelessRecipe,
) : CatalogItemStandardRecipe(recipe) {
    val inputItems: List<List<ItemRef>> = recipe.choiceList.map { it.toItems() }
    val outputItems: ItemRef = ItemRef.create(recipe.result)

    override val type = CatalogItemRecipeType.SHAPELESS_RECIPE
    override val sortId: String = recipe.key.value()
    override fun getLookupInputs(): Set<ItemRef> = inputItems.flatten().toSet()
}

class CatalogItemSmithingTransformRecipe(
    recipe: SmithingTransformRecipe,
) : CatalogItemStandardRecipe(recipe) {
    val baseItems: List<ItemRef> = recipe.base.toItems()
    val templateItems: List<ItemRef> = recipe.template.toItems()
    val additionItems: List<ItemRef> = recipe.addition.toItems()
    val outputItemRef: ItemRef = ItemRef.create(recipe.result)

    override val type = CatalogItemRecipeType.SMITHING_TRANSFORM_RECIPE
    override val sortId: String = recipe.key.value()
    override fun getLookupInputs(): Set<ItemRef> = (baseItems + templateItems + additionItems).toSet()
}

class CatalogItemSmithingTrimRecipe(
    recipe: SmithingTrimRecipe,
) : CatalogItemStandardRecipe(recipe) {
    val baseItems: List<ItemRef> = recipe.base.toItems()
    val templateItems: List<ItemRef> = recipe.template.toItems()
    val additionItems: List<ItemRef> = recipe.addition.toItems()

    override val type = CatalogItemRecipeType.SMITHING_TRIM_RECIPE
    override val sortId: String = recipe.key.value()
    override fun getLookupInputs(): Set<ItemRef> = (baseItems + templateItems + additionItems).toSet()

    // 锻造台纹饰配方没有输出, 返回一个占位物品作为节点
    override fun getLookupOutputs(): Set<ItemRef> = emptySet()
}

class CatalogItemSmokingRecipe(
    recipe: SmokingRecipe,
) : CatalogItemCookingRecipe(recipe) {
    override val type = CatalogItemRecipeType.SMOKING_RECIPE
    override val sortId: String = recipe.key.value()
}

class CatalogItemStonecuttingRecipe(
    recipe: StonecuttingRecipe,
) : CatalogItemStandardRecipe(recipe) {
    val inputItems: List<ItemRef> = recipe.inputChoice.toItems()
    val outputItem: ItemRef = ItemRef.create(recipe.result)

    override val type = CatalogItemRecipeType.STONECUTTING_RECIPE
    override val sortId: String = recipe.key.value()
    override fun getLookupInputs(): Set<ItemRef> = inputItems.toSet()
}

/**
 * 方便函数.
 */
private fun RecipeChoice?.toItems(): List<ItemRef> {
    return when (this) {
        is RecipeChoice.ExactChoice -> choices.mapNotNull { ItemRef.create(it) }
        is RecipeChoice.MaterialChoice -> choices.map { ItemRef.create(it) }
        else -> emptyList()
    }
}
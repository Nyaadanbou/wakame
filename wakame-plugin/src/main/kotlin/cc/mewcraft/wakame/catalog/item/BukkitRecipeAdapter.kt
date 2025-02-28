package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.wakame.core.ItemX
import cc.mewcraft.wakame.core.ItemXFactoryRegistry
import cc.mewcraft.wakame.core.ItemXNoOp
import cc.mewcraft.wakame.core.ItemXVanilla
import org.bukkit.inventory.*
import org.bukkit.inventory.Recipe as BukkitRecipe

abstract class BukkitRecipeAdapter(
    private val recipe: BukkitRecipe,
) : CatalogRecipe {

    fun <T : BukkitRecipe> recipe(): T = recipe as T

    // 缓存搜索时使用的索引, 即去重展开后的 ItemX 集合
    abstract val inputs: Set<ItemX>

    private val outputs: Set<ItemX> = setOfNotNull(ItemXFactoryRegistry[recipe.result])

    override fun getLookupInputs(): Set<ItemX> = inputs
    override fun getLookupOutputs(): Set<ItemX> = outputs
}

/**
 * 将 [BukkitRecipe] 转化为 [CatalogRecipe] 的适配器.
 * 返回 `null` 意味着无法转化, 即是一些图鉴不支持显示的特殊配方.
 */
internal fun convertToBukkitRecipeAdapter(recipe: BukkitRecipe): BukkitRecipeAdapter? {
    return when (recipe) {
        is BlastingRecipe -> BlastingRecipeAdapter(recipe)
        is CampfireRecipe -> CampfireRecipeAdapter(recipe)
        is FurnaceRecipe -> FurnaceRecipeAdapter(recipe)
        is ShapedRecipe -> ShapedRecipeAdapter(recipe)
        is ShapelessRecipe -> ShapelessRecipeAdapter(recipe)
        is SmithingTransformRecipe -> SmithingTransformRecipeAdapter(recipe)
        is SmithingTrimRecipe -> SmithingTrimRecipeAdapter(recipe)
        is SmokingRecipe -> SmokingRecipeAdapter(recipe)
        is StonecuttingRecipe -> StonecuttingRecipeAdapter(recipe)
        else -> null
    }
}

/**
 * 抽象的烧制配方.
 * 意在和原版一样减少一些重复代码.
 */
abstract class CookingRecipeAdapter(
    bukkitRecipe: CookingRecipe<*>,
) : BukkitRecipeAdapter(bukkitRecipe) {
    override val sortId: String = bukkitRecipe.key.value()

    // 对应原版该类型配方对RecipeChoice存储格式
    // 意在缓存各个RecipeChoice转化为List<ItemX>的结果
    val inputItems: List<ItemX> = convertChoiceToItemXList(bukkitRecipe.inputChoice)

    // 缓存配方的输出转化为 ItemX 的结果
    // TODO 通过改进 ItemX 使得这里不用强转非空
    val outputItems: ItemX = ItemXFactoryRegistry[bukkitRecipe.result]!!

    // 意在缓存 getInputs() 函数拿到的值
    override val inputs: Set<ItemX> = inputItems.toSet()
}


class BlastingRecipeAdapter(
    bukkitRecipe: BlastingRecipe,
) : CookingRecipeAdapter(bukkitRecipe) {
    override val type = CatalogRecipeType.BLASTING_RECIPE
}

class CampfireRecipeAdapter(
    bukkitRecipe: CampfireRecipe,
) : CookingRecipeAdapter(bukkitRecipe) {
    override val type = CatalogRecipeType.CAMPFIRE_RECIPE
}

class FurnaceRecipeAdapter(
    bukkitRecipe: FurnaceRecipe,
) : CookingRecipeAdapter(bukkitRecipe) {
    override val type = CatalogRecipeType.FURNACE_RECIPE
}

class ShapedRecipeAdapter(
    bukkitRecipe: ShapedRecipe,
) : BukkitRecipeAdapter(bukkitRecipe) {
    val inputItems: Map<Char, List<ItemX>> = bukkitRecipe.choiceMap.mapValues { convertChoiceToItemXList(it.value) }
    val outputItem: ItemX = ItemXFactoryRegistry[bukkitRecipe.result]!!

    override val type = CatalogRecipeType.SHAPED_RECIPE
    override val sortId: String = bukkitRecipe.key.value()
    override val inputs: Set<ItemX> = inputItems.values.flatten().toSet()
}

class ShapelessRecipeAdapter(
    bukkitRecipe: ShapelessRecipe,
) : BukkitRecipeAdapter(bukkitRecipe) {
    val inputItems: List<List<ItemX>> = bukkitRecipe.choiceList.map { convertChoiceToItemXList(it) }
    val outputItems: ItemX = ItemXFactoryRegistry[bukkitRecipe.result]!!

    override val type = CatalogRecipeType.SHAPELESS_RECIPE
    override val sortId: String = bukkitRecipe.key.value()
    override val inputs: Set<ItemX> = inputItems.flatten().toSet()
}

class SmithingTransformRecipeAdapter(
    bukkitRecipe: SmithingTransformRecipe,
) : BukkitRecipeAdapter(bukkitRecipe) {
    val baseItems: List<ItemX> = convertChoiceToItemXList(bukkitRecipe.base)
    val templateItems: List<ItemX> = convertChoiceToItemXList(bukkitRecipe.template)
    val additionItems: List<ItemX> = convertChoiceToItemXList(bukkitRecipe.addition)
    val outputItemX: ItemX = ItemXFactoryRegistry[bukkitRecipe.result]!!

    override val type = CatalogRecipeType.SMITHING_TRANSFORM_RECIPE
    override val sortId: String = bukkitRecipe.key.value()
    override val inputs: Set<ItemX> = (baseItems + templateItems + additionItems).toSet()
}

class SmithingTrimRecipeAdapter(
    bukkitRecipe: SmithingTrimRecipe,
) : BukkitRecipeAdapter(bukkitRecipe) {
    val baseItems: List<ItemX> = convertChoiceToItemXList(bukkitRecipe.base)
    val templateItems: List<ItemX> = convertChoiceToItemXList(bukkitRecipe.template)
    val additionItems: List<ItemX> = convertChoiceToItemXList(bukkitRecipe.addition)

    override val type = CatalogRecipeType.SMITHING_TRIM_RECIPE
    override val sortId: String = bukkitRecipe.key.value()
    override val inputs: Set<ItemX> = (baseItems + templateItems + additionItems).toSet()

    // 锻造台纹饰配方没有输出, 返回一个占位物品作为节点
    override fun getLookupOutputs(): Set<ItemX> {
        return setOf(ItemXNoOp)
    }
}

class SmokingRecipeAdapter(
    bukkitRecipe: SmokingRecipe,
) : CookingRecipeAdapter(bukkitRecipe) {
    override val type = CatalogRecipeType.SMOKING_RECIPE
    override val sortId: String = bukkitRecipe.key.value()
}

class StonecuttingRecipeAdapter(
    bukkitRecipe: StonecuttingRecipe,
) : BukkitRecipeAdapter(bukkitRecipe) {
    val inputItems: List<ItemX> = convertChoiceToItemXList(bukkitRecipe.inputChoice)
    val outputItem: ItemX = ItemXFactoryRegistry[bukkitRecipe.result]!!

    override val type = CatalogRecipeType.STONECUTTING_RECIPE
    override val sortId: String = bukkitRecipe.key.value()
    override val inputs: Set<ItemX> = inputItems.toSet()
}

/**
 * 方便函数.
 */
private fun convertChoiceToItemXList(choice: RecipeChoice?): List<ItemX> {
    return when (choice) {
        is RecipeChoice.ExactChoice -> choice.choices.mapNotNull { ItemXFactoryRegistry[it] }
        is RecipeChoice.MaterialChoice -> choice.choices.map { ItemXVanilla(it.toString().lowercase()) }
        else -> emptyList()
    }
}
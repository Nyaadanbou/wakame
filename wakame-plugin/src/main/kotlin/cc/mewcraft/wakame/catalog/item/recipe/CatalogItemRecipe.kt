package cc.mewcraft.wakame.catalog.item.recipe

import cc.mewcraft.wakame.item.ItemRef
import xyz.xenondevs.invui.gui.Gui

/**
 * 在物品图鉴中记录的具体“配方”.
 *
 * 这个“配方”是广义的, 即只要是实现了这个接口的物品获取方式, 就可以在图鉴中作为配方被搜索.
 */
interface CatalogItemRecipe {

    val type: CatalogItemRecipeType

    /**
     * 获取配方排序时使用的唯一标识.
     */
    val sortId: String

    /**
     * 获取配方输入物品展平去重后的物品集合, 用于检索时作为节点.
     * 只会在网络构建时被调用一次.
     */
    fun getLookupInputs(): Set<ItemRef>

    /**
     * 获取配方输出物品展平去重后的物品集合, 用于检索时作为节点.
     * 只会在网络构建时被调用一次.
     */
    fun getLookupOutputs(): Set<ItemRef>

}

data class CatalogItemRecipeType(
    val name: String,
    /**
     * [CatalogItemRecipe] 的各实现的 [Gui] 在图鉴展示时的优先级. 数字小的类型将被排在前面.
     */
    val sortPriority: Int,
) {
    companion object {
        val SHAPED_RECIPE = CatalogItemRecipeType("shaped_recipe", 100)
        val SHAPELESS_RECIPE = CatalogItemRecipeType("shapeless_recipe", 200)
        val FURNACE_RECIPE = CatalogItemRecipeType("furnace_recipe", 300)
        val SMOKING_RECIPE = CatalogItemRecipeType("smoking_recipe", 400)
        val BLASTING_RECIPE = CatalogItemRecipeType("blasting_recipe", 500)
        val CAMPFIRE_RECIPE = CatalogItemRecipeType("campfire_recipe", 600)
        val SMITHING_TRANSFORM_RECIPE = CatalogItemRecipeType("smithing_transform_recipe", 700)
        val SMITHING_TRIM_RECIPE = CatalogItemRecipeType("smithing_trim_recipe", 800)
        val STONECUTTING_RECIPE = CatalogItemRecipeType("stonecutting_recipe", 900)
        val LOOT_TABLE_RECIPE = CatalogItemRecipeType("loot_table_recipe", 1000)
    }
}
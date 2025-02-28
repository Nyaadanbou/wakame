package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.wakame.core.ItemX

/**
 * 在物品图鉴中记录的具体“配方”.
 *
 * 这个“配方”是广义的, 即只要是实现了这个接口的物品获取方式, 就可以在图鉴中作为配方被搜索.
 */
interface CatalogRecipe {

    val type: CatalogRecipeType

    /**
     * 获取配方输入物品展平去重后的 [ItemX] 集合.
     * 用于检索时作为索引.
     */
    fun getLookupInputs(): Set<ItemX>

    /**
     * 获取配方输出物品展平去重后的 [ItemX] 集合.
     * 用于检索时作为索引.
     */
    fun getLookupOutputs(): Set<ItemX>

}

enum class CatalogRecipeType {
    BLASTING_RECIPE,
    CAMPFIRE_RECIPE,
    FURNACE_RECIPE,
    SHAPED_RECIPE,
    SHAPELESS_RECIPE,
    SMITHING_TRANSFORM_RECIPE,
    SMITHING_TRIM_RECIPE,
    SMOKING_RECIPE,
    STONECUTTING_RECIPE,
}
package cc.mewcraft.wakame.catalog.item.node

import cc.mewcraft.wakame.item.ItemRef
import xyz.xenondevs.invui.gui.Gui

/**
 * 在物品图鉴中记录的节点.
 *
 * 实现了这个接口的物品获取方式, 就可以在图鉴中作为节点被搜索.
 */
interface CatalogItemNode {

    val type: CatalogItemNodeType

    /**
     * 获取节点排序时使用的唯一标识.
     */
    val sortId: String

    /**
     * 获取节点输入物品展平去重后的物品集合, 用于检索时作为节点.
     * 只会在网络构建时被调用一次.
     */
    fun getLookupInputs(): Set<ItemRef>

    /**
     * 获取节点输出物品展平去重后的物品集合, 用于检索时作为节点.
     * 只会在网络构建时被调用一次.
     */
    fun getLookupOutputs(): Set<ItemRef>
}

data class CatalogItemNodeType(
    val name: String,
    /**
     * [CatalogItemNode] 的各实现的 [Gui] 在图鉴展示时的优先级. 数字小的类型将被排在前面.
     */
    val sortPriority: Int,
) {
    companion object {
        val SHAPED_RECIPE = CatalogItemNodeType("shaped_recipe", 100)
        val SHAPELESS_RECIPE = CatalogItemNodeType("shapeless_recipe", 200)
        val FURNACE_RECIPE = CatalogItemNodeType("furnace_recipe", 300)
        val SMOKING_RECIPE = CatalogItemNodeType("smoking_recipe", 400)
        val BLASTING_RECIPE = CatalogItemNodeType("blasting_recipe", 500)
        val CAMPFIRE_RECIPE = CatalogItemNodeType("campfire_recipe", 600)
        val SMITHING_TRANSFORM_RECIPE = CatalogItemNodeType("smithing_transform_recipe", 700)
        val SMITHING_TRIM_RECIPE = CatalogItemNodeType("smithing_trim_recipe", 800)
        val STONECUTTING_RECIPE = CatalogItemNodeType("stonecutting_recipe", 900)
        val LOOT_TABLE = CatalogItemNodeType("loot_table", 1000)
        val CRAFTING_STATION = CatalogItemNodeType("crafting_station", 1100)
    }
}
package cc.mewcraft.wakame.catalog.item.node

import cc.mewcraft.wakame.craftingstation.recipe.*
import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.item.ItemRef

/**
 * 代表一个合成站配方形式的节点.
 *
 * 合成站配方是多对一的关系: 多种输入物品合成一种输出物品.
 */
data class CatalogItemCraftingStationNode(
    /**
     * 合成站的唯一标识.
     */
    val stationId: String,

    /**
     * 合成站配方实例.
     */
    val recipe: Recipe,

    /**
     * 该配方在图鉴中展示时的菜单布局.
     */
    val menuCfg: BasicMenuSettings,
) : CatalogItemNode {

    override val type: CatalogItemNodeType =
        CatalogItemNodeType.CRAFTING_STATION
    override val sortId: String
        get() = "${stationId}/${recipe.key}"

    /**
     * 配方的所有输入选择.
     */
    val inputChoices: List<RecipeChoice> = recipe.input

    /**
     * 配方的输出结果.
     */
    val outputResult: RecipeResult = recipe.output

    /**
     * 从配方输入中提取的物品引用列表 (仅包含 ItemChoice, 忽略 ExpChoice 等).
     */
    private val inputItemRefs: Set<ItemRef> = recipe.input
        .filterIsInstance<ItemChoice>()
        .map { it.item }
        .toSet()

    /**
     * 从配方输出中提取的物品引用 (仅包含 ItemResult).
     */
    private val outputItemRefs: Set<ItemRef> = when (val output = recipe.output) {
        is ItemResult -> setOf(output.item)
    }

    override fun getLookupInputs(): Set<ItemRef> {
        return inputItemRefs
    }

    override fun getLookupOutputs(): Set<ItemRef> {
        return outputItemRefs
    }
}
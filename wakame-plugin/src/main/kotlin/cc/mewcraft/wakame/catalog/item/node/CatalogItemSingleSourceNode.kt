package cc.mewcraft.wakame.catalog.item.node

import cc.mewcraft.wakame.gui.BasicMenuSettings
import cc.mewcraft.wakame.item.ItemRef
import net.kyori.adventure.key.Key

/**
 * 代表一个自定义单源节点.
 *
 * 单源节点没有实际的"输入物品", 仅有一个虚拟图标代表来源 (例如怪物掉落, 盲盒, 任务奖励等).
 * 输出物品由配置文件手动指定.
 */
data class CatalogItemSingleSourceNode(
    /**
     * 单源节点的唯一标识.
     * 即配置文件相对于 `configs/catalog/item/node/single_source/` 的路径 (不含后缀).
     */
    val nodeId: String,

    /**
     * 节点的显示名称.
     */
    val displayName: String,

    /**
     * 该节点输出的物品列表.
     */
    val outputItems: List<ItemRef>,

    /**
     * 该节点在图鉴中展示时输入物品位置展示的图标.
     */
    val inputIcon: Key,

    /**
     * 该节点在图鉴中展示时的菜单布局.
     */
    val menuCfg: BasicMenuSettings,
) : CatalogItemNode {

    override val type: CatalogItemNodeType =
        CatalogItemNodeType.SINGLE_SOURCE

    override val sortId: String
        get() = nodeId

    override fun getLookupInputs(): Set<ItemRef> {
        return emptySet() // 单源节点没有实际的输入物品
    }

    override fun getLookupOutputs(): Set<ItemRef> {
        return outputItems.toSet()
    }
}
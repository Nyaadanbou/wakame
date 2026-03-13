@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.catalog.item.node.CatalogItemNode
import cc.mewcraft.wakame.event.map.CraftingStationRegistrationDoneEvent
import cc.mewcraft.wakame.event.map.MinecraftRecipeRegistrationDoneEvent
import cc.mewcraft.wakame.item.ItemRef
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry.DynamicRegistries
import cc.mewcraft.wakame.util.eventbus.MapEventBus
import com.google.common.graph.ImmutableNetwork
import com.google.common.graph.MutableNetwork
import com.google.common.graph.NetworkBuilder
import java.util.*

@Init(InitStage.POST_WORLD)
object CatalogItemNetwork {
    private lateinit var network: ImmutableNetwork<Optional<ItemRef>, CatalogItemNodeEdge>

    @InitFun
    fun init() {
        MapEventBus.subscribe<MinecraftRecipeRegistrationDoneEvent> {
            // 当原版配方注册完成时 -> 重建图鉴物品网络
            // 改进的地方 2026/03/10: 比这更好的时机?
            rebuildNetwork()
        }
        MapEventBus.subscribe<CraftingStationRegistrationDoneEvent> {
            // 合成站在启动时是延迟加载的, 这里需要在其就绪后再次重建网络
            // 改进的地方 2026/03/13: 比这更好的时机?
            rebuildNetwork()
        }
    }

    /**
     * 获取特定物品的所有获取方式 (来源).
     */
    fun getSource(node: ItemRef): Set<CatalogItemNode> {
        if (!network.nodes().contains(Optional.of(node))) return emptySet()
        return network.inEdges(Optional.of(node)).map(CatalogItemNodeEdge::node).toSet()
    }

    /**
     * 获取特定物品的所有可参与制作 (用途).
     */
    fun getUsage(node: ItemRef): Set<CatalogItemNode> {
        if (!network.nodes().contains(Optional.of(node))) return emptySet()
        return network.outEdges(Optional.of(node)).map(CatalogItemNodeEdge::node).toSet()
    }

    /**
     * 重建"来源&用途"网络.
     */
    fun rebuildNetwork() {
        CatalogItemCraftingStationNodeInitializer.reload()
        CatalogItemLootTableNodeInitializer.reload()
        CatalogItemRecipeNodeInitializer.reload()
        CatalogItemSingleSourceNodeInitializer.reload()

        network = buildNetWork()
    }

    private fun buildNetWork(): ImmutableNetwork<Optional<ItemRef>, CatalogItemNodeEdge> {
        LOGGER.info("Building catalog item network")

        // 自循环和平行边都是需要的, 举例说明:
        // 自循环: 锻造模板复制有序合成配方(模板+钻石等->模板*2)
        // 平行边: 淡灰色染料无序合成配方(白色染料+灰色染料->淡灰色染料*2 白色染料*2+黑色染料->灰色染料*3)
        val network = NetworkBuilder
            .directed()
            .allowsSelfLoops(true)
            .allowsParallelEdges(true)
            .build<Optional<ItemRef>, CatalogItemNodeEdge>()

        // 添加起占位作用的空节点
        network.addNode(Optional.empty())

        // Koish 合成站配方
        for (craftingStationNode in DynamicRegistries.CATALOG_ITEM_CRAFTING_STATION_NODE) {
            network.addNode(craftingStationNode)
        }

        // Minecraft 战利品表
        for (lootTableNode in DynamicRegistries.CATALOG_ITEM_MINECRAFT_LOOT_TABLE_NODE) {
            network.addNode(lootTableNode)
        }

        // Minecraft 合成配方
        for (recipeNode in DynamicRegistries.CATALOG_ITEM_MINECRAFT_RECIPE_NODE) {
            network.addNode(recipeNode)
        }

        // 自定义单源节点
        for (mythicDropNode in DynamicRegistries.CATALOG_ITEM_SINGLE_SOURCE_NODE) {
            network.addNode(mythicDropNode)
        }

        return ImmutableNetwork.copyOf(network)
    }

    /**
     * 方便函数.
     * 当配方的输入输出为空时会使用空节点占位.
     */
    private fun MutableNetwork<Optional<ItemRef>, CatalogItemNodeEdge>.addNode(catalogItemNode: CatalogItemNode) {
        val lookupInputs = catalogItemNode.getLookupInputs()
        val lookupOutputs = catalogItemNode.getLookupOutputs()
        if (lookupInputs.isEmpty()) {
            for (outputNode in lookupOutputs) {
                addNode(Optional.of(outputNode))
                addEdge(Optional.empty(), Optional.of(outputNode), CatalogItemNodeEdge(catalogItemNode))
            }
            return
        }
        if (lookupOutputs.isEmpty()) {
            for (inputNode in lookupInputs) {
                addNode(Optional.of(inputNode))
                addEdge(Optional.of(inputNode), Optional.empty(), CatalogItemNodeEdge(catalogItemNode))
            }
            return
        }
        for (inputNode in lookupInputs) {
            for (outputNode in lookupOutputs) {
                addNode(Optional.of(inputNode))
                addNode(Optional.of(outputNode))
                addEdge(Optional.of(inputNode), Optional.of(outputNode), CatalogItemNodeEdge(catalogItemNode))
            }
        }
    }

    /**
     * 封装一个 [CatalogItemNode] 作为 [ImmutableNetwork] 的边.
     */
    private class CatalogItemNodeEdge(
        val node: CatalogItemNode,
    )
}

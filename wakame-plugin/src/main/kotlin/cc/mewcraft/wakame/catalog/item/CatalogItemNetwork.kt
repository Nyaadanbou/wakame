@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.catalog.item.node.CatalogItemNode
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
    private lateinit var network: ImmutableNetwork<Optional<ItemRef>, CatalogRecipeEdge>

    @InitFun
    fun init() {
        MapEventBus.subscribe<MinecraftRecipeRegistrationDoneEvent> {
            // 当原版配方注册完成时 -> 重建图鉴物品网络
            // 改进的地方 2026/03/10: 比这更好的时机?
            rebuildNetwork()
        }
    }

    /**
     * 获取特定物品的所有获取方式 (来源).
     */
    fun getSource(node: ItemRef): Set<CatalogItemNode> {
        if (!network.nodes().contains(Optional.of(node))) return emptySet()
        return network.inEdges(Optional.of(node)).map(CatalogRecipeEdge::recipe).toSet()
    }

    /**
     * 获取特定物品的所有可参与制作 (用途).
     */
    fun getUsage(node: ItemRef): Set<CatalogItemNode> {
        if (!network.nodes().contains(Optional.of(node))) return emptySet()
        return network.outEdges(Optional.of(node)).map(CatalogRecipeEdge::recipe).toSet()
    }

    /**
     * 重建"来源&用途"网络.
     */
    fun rebuildNetwork() {
        CatalogItemCraftingStationNodeInitializer.reload()
        CatalogItemCrateNodeInitializer.reload()
        CatalogItemLootTableNodeInitializer.reload()
        CatalogItemMythicDropNodeInitializer.reload()
        CatalogItemQuestNodeInitializer.reload()
        CatalogItemSignupNodeInitializer.reload()
        CatalogItemRecipeNodeInitializer.reload()

        network = buildNetWork()
    }

    private fun buildNetWork(): ImmutableNetwork<Optional<ItemRef>, CatalogRecipeEdge> {
        LOGGER.info("Building catalog item recipe network")

        // 自循环和平行边都是需要的, 举例说明:
        // 自循环: 锻造模板复制有序合成配方(模板+钻石等->模板*2)
        // 平行边: 淡灰色染料无序合成配方(白色染料+灰色染料->淡灰色染料*2 白色染料*2+黑色染料->灰色染料*3)
        val network = NetworkBuilder
            .directed()
            .allowsSelfLoops(true)
            .allowsParallelEdges(true)
            .build<Optional<ItemRef>, CatalogRecipeEdge>()

        // 添加起占位作用的空节点
        network.addNode(Optional.empty())

        // 合成站
        for (craftingStationRecipe in DynamicRegistries.CATALOG_ITEM_CRAFTING_STATION_RECIPE) {
            network.addRecipe(craftingStationRecipe)
        }

        // 盲盒
        for (crateRecipe in DynamicRegistries.CATALOG_ITEM_CRATE_RECIPE) {
            network.addRecipe(crateRecipe)
        }

        // 原版战利品表
        for (lootTableRecipe in DynamicRegistries.CATALOG_ITEM_LOOT_TABLE_RECIPE) {
            network.addRecipe(lootTableRecipe)
        }

        // MythicMobs 生物掉落
        for (mythicDropRecipe in DynamicRegistries.CATALOG_ITEM_MYTHIC_DROP_RECIPE) {
            network.addRecipe(mythicDropRecipe)
        }

        // NPC 任务奖励
        for (questRecipe in DynamicRegistries.CATALOG_ITEM_QUEST_RECIPE) {
            network.addRecipe(questRecipe)
        }

        // 签到奖励
        for (signupRecipe in DynamicRegistries.CATALOG_ITEM_SIGNUP_RECIPE) {
            network.addRecipe(signupRecipe)
        }

        // 原版合成配方
        for (standardRecipe in DynamicRegistries.CATALOG_ITEM_STANDARD_RECIPE) {
            network.addRecipe(standardRecipe)
        }

        return ImmutableNetwork.copyOf(network)
    }

    /**
     * 方便函数.
     * 当配方的输入输出为空时会使用空节点占位.
     */
    private fun MutableNetwork<Optional<ItemRef>, CatalogRecipeEdge>.addRecipe(catalogItemNode: CatalogItemNode) {
        val lookupInputs = catalogItemNode.getLookupInputs()
        val lookupOutputs = catalogItemNode.getLookupOutputs()
        if (lookupInputs.isEmpty()) {
            for (outputNode in lookupOutputs) {
                addNode(Optional.of(outputNode))
                addEdge(Optional.empty(), Optional.of(outputNode), CatalogRecipeEdge(catalogItemNode))
            }
            return
        }
        if (lookupOutputs.isEmpty()) {
            for (inputNode in lookupInputs) {
                addNode(Optional.of(inputNode))
                addEdge(Optional.of(inputNode), Optional.empty(), CatalogRecipeEdge(catalogItemNode))
            }
            return
        }
        for (inputNode in lookupInputs) {
            for (outputNode in lookupOutputs) {
                addNode(Optional.of(inputNode))
                addNode(Optional.of(outputNode))
                addEdge(Optional.of(inputNode), Optional.of(outputNode), CatalogRecipeEdge(catalogItemNode))
            }
        }
    }

    /**
     * 封装一个 [CatalogItemNode] 作为 [ImmutableNetwork] 的边.
     */
    private class CatalogRecipeEdge(
        val recipe: CatalogItemNode,
    )
}

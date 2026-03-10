@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.catalog.item.recipe.*
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
import org.bukkit.Bukkit
import org.bukkit.inventory.*
import java.util.*
import org.bukkit.inventory.Recipe as BukkitRecipe

@Init(InitStage.POST_WORLD)
object CatalogItemRecipeNetwork {

    private lateinit var network: ImmutableNetwork<Optional<ItemRef>, CatalogRecipeEdge>

    @InitFun
    fun init() {
        // 当原版配方注册完成时 -> 重建网络
        MapEventBus.subscribe<MinecraftRecipeRegistrationDoneEvent> { rebuildNetwork() }
    }

    /**
     * 获取特定物品的所有获取方式 (来源).
     */
    fun getSource(node: ItemRef): Set<CatalogItemRecipe> {
        if (!network.nodes().contains(Optional.of(node))) return emptySet()
        return network.inEdges(Optional.of(node)).map(CatalogRecipeEdge::recipe).toSet()
    }

    /**
     * 获取特定物品的所有可参与制作 (用途).
     */
    fun getUsage(node: ItemRef): Set<CatalogItemRecipe> {
        if (!network.nodes().contains(Optional.of(node))) return emptySet()
        return network.outEdges(Optional.of(node)).map(CatalogRecipeEdge::recipe).toSet()
    }

    private fun rebuildNetwork() {
        network = buildNetWork()
    }

    private fun buildNetWork(): ImmutableNetwork<Optional<ItemRef>, CatalogRecipeEdge> {
        LOGGER.info("Building catalog recipe network")
        // 自循环和平行边都是需要的, 举例说明:
        // 自循环: 锻造模板复制有序合成配方(模板+钻石等->模板*2)
        // 平行边: 淡灰色染料无序合成配方(白色染料+灰色染料->淡灰色染料*2 白色染料*2+黑色染料->灰色染料*3)
        val network: MutableNetwork<Optional<ItemRef>, CatalogRecipeEdge> = NetworkBuilder
            .directed()
            .allowsSelfLoops(true)
            .allowsParallelEdges(true)
            .build()

        // 添加起占位作用的空节点
        network.addNode(Optional.empty())

        // 原版配方
        for (bukkitRecipe in Bukkit.recipeIterator()) {
            // 为空意味着是图鉴无法显示的原版特殊配方, 直接跳过
            val catalogBukkitRecipe = bukkitRecipe.toCatalogRecipe() ?: continue
            network.addRecipe(catalogBukkitRecipe)
        }

        // 战利品表配方
        for (lootTableRecipe in DynamicRegistries.CATALOG_ITEM_LOOT_TABLE_RECIPE) {
            network.addRecipe(lootTableRecipe)
        }

        // TODO 合成站配方
        // TODO MythicMobs 生物掉落
        // TODO NPC
        // TODO 签到
        // TODO 盲盒

        return ImmutableNetwork.copyOf(network)
    }

    /**
     * 方便函数.
     * 返回 `null` 意味着无法转化, 即图鉴不支持显示的特殊配方.
     */
    private fun BukkitRecipe.toCatalogRecipe(): CatalogItemStandardRecipe? {
        return when (this) {
            is BlastingRecipe -> CatalogItemBlastingRecipe(this)
            is CampfireRecipe -> CatalogItemCampfireRecipe(this)
            is FurnaceRecipe -> CatalogItemFurnaceRecipe(this)
            is ShapedRecipe -> CatalogItemShapedRecipe(this)
            is ShapelessRecipe -> CatalogItemShapelessRecipe(this)
            is SmithingTransformRecipe -> CatalogItemSmithingTransformRecipe(this)
            is SmithingTrimRecipe -> CatalogItemSmithingTrimRecipe(this)
            is SmokingRecipe -> CatalogItemSmokingRecipe(this)
            is StonecuttingRecipe -> CatalogItemStonecuttingRecipe(this)
            else -> null
        }
    }

    /**
     * 方便函数.
     * 当配方的输入输出为空时会使用空节点占位.
     */
    private fun MutableNetwork<Optional<ItemRef>, CatalogRecipeEdge>.addRecipe(catalogItemRecipe: CatalogItemRecipe) {
        val lookupInputs = catalogItemRecipe.getLookupInputs()
        val lookupOutputs = catalogItemRecipe.getLookupOutputs()

        if (lookupInputs.isEmpty()) {
            for (outputNode in lookupOutputs) {
                addNode(Optional.of(outputNode))
                addEdge(Optional.empty(), Optional.of(outputNode), CatalogRecipeEdge(catalogItemRecipe))
            }
            return
        }

        if (lookupOutputs.isEmpty()) {
            for (inputNode in lookupInputs) {
                addNode(Optional.of(inputNode))
                addEdge(Optional.of(inputNode), Optional.empty(), CatalogRecipeEdge(catalogItemRecipe))
            }
            return
        }

        for (inputNode in lookupInputs) {
            for (outputNode in lookupOutputs) {
                addNode(Optional.of(inputNode))
                addNode(Optional.of(outputNode))
                addEdge(Optional.of(inputNode), Optional.of(outputNode), CatalogRecipeEdge(catalogItemRecipe))
            }
        }
    }

    /**
     * 封装一个 [CatalogItemRecipe] 作为 [ImmutableNetwork] 的边.
     */
    private class CatalogRecipeEdge(
        val recipe: CatalogItemRecipe,
    )
}

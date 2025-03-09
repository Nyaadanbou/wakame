package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.catalog.item.recipe.*
import cc.mewcraft.wakame.core.ItemX
import cc.mewcraft.wakame.event.map.MinecraftRecipeRegistrationDoneEvent
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.util.eventbus.MapEventBus
import com.google.common.graph.ImmutableNetwork
import com.google.common.graph.MutableNetwork
import com.google.common.graph.NetworkBuilder
import org.bukkit.Bukkit
import org.bukkit.inventory.*
import org.bukkit.inventory.Recipe as BukkitRecipe

@Init(stage = InitStage.POST_WORLD)
object CatalogItemRecipeNetwork {

    private lateinit var network: ImmutableNetwork<ItemX, CatalogRecipeEdge>

    /**
     * 获取特定物品的所有获取方式 (来源).
     */
    fun getSource(node: ItemX): Set<CatalogRecipe> {
        if (!network.nodes().contains(node)) return emptySet()
        return network.inEdges(node).map(CatalogRecipeEdge::recipe).toSet()
    }

    /**
     * 获取特定物品的所有可参与制作 (用途).
     */
    fun getUsage(node: ItemX): Set<CatalogRecipe> {
        if (!network.nodes().contains(node)) return emptySet()
        return network.outEdges(node).map(CatalogRecipeEdge::recipe).toSet()
    }

    /**
     * 封装一个 [CatalogRecipe] 作为 [ImmutableNetwork] 的边.
     */
    private class CatalogRecipeEdge(
        val recipe: CatalogRecipe,
    )

    @InitFun
    fun init() {

        // 当原版配方注册完成时 -> 重建网络
        MapEventBus.subscribe<MinecraftRecipeRegistrationDoneEvent> {
            rebuildNetwork()
        }

    }

    private fun rebuildNetwork() {
        network = buildNetWork()
    }

    private fun buildNetWork(): ImmutableNetwork<ItemX, CatalogRecipeEdge> {
        LOGGER.info("Building catalog recipe network")
        // 自循环和平行边都是需要的, 举例说明:
        // 自循环: 锻造模板复制有序合成配方(模板+钻石等->模板*2)
        // 平行边: 淡灰色染料无序合成配方(白色染料+灰色染料->淡灰色染料*2 白色染料*2+黑色染料->灰色染料*3)
        val network: MutableNetwork<ItemX, CatalogRecipeEdge> = NetworkBuilder
            .directed()
            .allowsSelfLoops(true)
            .allowsParallelEdges(true)
            .build()

        // 原版配方
        for (bukkitRecipe in Bukkit.recipeIterator()) {
            // 为空意味着是图鉴无法显示的原版特殊配方, 直接跳过
            val catalogBukkitRecipe = bukkitRecipe.toCatalogRecipe() ?: continue
            network.addRecipe(catalogBukkitRecipe, "BukkitRecipe '${bukkitRecipe::class}' has no input or output")
        }

        // 战利品表配方
        for (lootTableRecipe in KoishRegistries.LOOT_TABLE_RECIPE) {
            network.addRecipe(lootTableRecipe)
        }

        // TODO 工作站配方

        return ImmutableNetwork.copyOf(network)
    }

    /**
     * 方便函数.
     *
     * 返回 `null` 意味着无法转化, 即是一些图鉴不支持显示的特殊配方.
     */
    private fun BukkitRecipe.toCatalogRecipe(): CatalogStandardRecipe? {
        return when (this) {
            is BlastingRecipe -> CatalogBlastingRecipe(this)
            is CampfireRecipe -> CatalogCampfireRecipe(this)
            is FurnaceRecipe -> CatalogFurnaceRecipe(this)
            is ShapedRecipe -> CatalogShapedRecipe(this)
            is ShapelessRecipe -> CatalogShapelessRecipe(this)
            is SmithingTransformRecipe -> CatalogSmithingTransformRecipe(this)
            is SmithingTrimRecipe -> CatalogSmithingTrimRecipe(this)
            is SmokingRecipe -> CatalogSmokingRecipe(this)
            is StonecuttingRecipe -> CatalogStonecuttingRecipe(this)
            else -> null
        }
    }

    /**
     * 方便函数.
     * 输入输出存在空时会抛异常.
     */
    private fun MutableNetwork<ItemX, CatalogRecipeEdge>.addRecipe(
        catalogRecipe: CatalogRecipe,
        errorMessage: String,
    ) {
        // 当配方输入和输出均非空时才会添加节点和边, 正常情况下不应该出现
        val lookupInputs = catalogRecipe.getLookupInputs()
        val lookupOutputs = catalogRecipe.getLookupOutputs()
        if (lookupInputs.isEmpty() || lookupOutputs.isEmpty()) {
            LOGGER.error(errorMessage)
            return
        }

        for (inputNode in lookupInputs) {
            for (outputNode in lookupOutputs) {
                addNode(inputNode)
                addNode(outputNode)
                addEdge(inputNode, outputNode, CatalogRecipeEdge(catalogRecipe))
            }
        }
    }

    /**
     * 方便函数.
     * 输入输出存在空时仅跳过.
     */
    private fun MutableNetwork<ItemX, CatalogRecipeEdge>.addRecipe(
        catalogRecipe: CatalogRecipe
    ) {
        val lookupInputs = catalogRecipe.getLookupInputs()
        val lookupOutputs = catalogRecipe.getLookupOutputs()
        if (lookupInputs.isEmpty() || lookupOutputs.isEmpty()) {
            return
        }

        for (inputNode in lookupInputs) {
            for (outputNode in lookupOutputs) {
                addNode(inputNode)
                addNode(outputNode)
                addEdge(inputNode, outputNode, CatalogRecipeEdge(catalogRecipe))
            }
        }
    }

}

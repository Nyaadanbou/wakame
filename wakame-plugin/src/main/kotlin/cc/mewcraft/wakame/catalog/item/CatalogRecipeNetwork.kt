package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.catalog.item.recipe.convertToBukkitRecipeAdapter
import cc.mewcraft.wakame.core.ItemX
import cc.mewcraft.wakame.event.map.MinecraftRecipeRegistrationDoneEvent
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.util.eventbus.MapEventBus
import com.google.common.graph.ImmutableNetwork
import com.google.common.graph.MutableNetwork
import com.google.common.graph.Network
import com.google.common.graph.NetworkBuilder
import org.bukkit.Bukkit

@Init(stage = InitStage.POST_WORLD)
object CatalogRecipeNetwork {

    private lateinit var network: ImmutableNetwork<ItemX, CatalogRecipeEdge>

    @InitFun
    private fun init() {
        // 当原版配方注册完成时 -> 重建网络
        MapEventBus.subscribe<MinecraftRecipeRegistrationDoneEvent> { rebuildNetwork() }
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
            val bukkitRecipeAdapter = convertToBukkitRecipeAdapter(bukkitRecipe)
                ?: continue

            addRecipeToNetwork(
                network, bukkitRecipeAdapter,
                "Recipe type '${bukkitRecipe::class}' has no input or output"
            )
        }

        // 战利品表配方
        for (lootTableRecipe in KoishRegistries.LOOT_TABLE_RECIPE) {
            addRecipeToNetwork(
                network, lootTableRecipe,
                "Loot table recipe '${lootTableRecipe.lootTableId}' has no input or output"
            )
        }

        // TODO 工作站配方

        return ImmutableNetwork.copyOf(network)
    }

    /**
     * 方便函数.
     */
    private fun addRecipeToNetwork(
        network: MutableNetwork<ItemX, CatalogRecipeEdge>,
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
                network.addNode(inputNode)
                network.addNode(outputNode)
                network.addEdge(inputNode, outputNode, CatalogRecipeEdge(catalogRecipe))
            }
        }
    }

    /**
     * 获取特定物品的所有获取方式 (来源).
     */
    fun getSource(node: ItemX): Set<CatalogRecipe> {
        if (!network.nodes().contains(node)) return emptySet()
        return network.inEdges(node).map(CatalogRecipeEdge::catalogRecipe).toSet()
    }

    /**
     * 获取特定物品的所有可参与制作 (用途).
     */
    fun getUsage(node: ItemX): Set<CatalogRecipe> {
        if (!network.nodes().contains(node)) return emptySet()
        return network.outEdges(node).map(CatalogRecipeEdge::catalogRecipe).toSet()
    }

    /**
     * 封装一个 [CatalogRecipe] 作为 [Network] 的边.
     */
    class CatalogRecipeEdge(
        val catalogRecipe: CatalogRecipe,
    )

}

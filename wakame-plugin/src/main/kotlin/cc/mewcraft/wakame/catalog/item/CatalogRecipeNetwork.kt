package cc.mewcraft.wakame.catalog.item

import cc.mewcraft.wakame.core.ItemX
import cc.mewcraft.wakame.event.map.MinecraftRecipeRegistrationDoneEvent
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.eventbus.MapEventBus
import com.google.common.graph.MutableNetwork
import com.google.common.graph.Network
import com.google.common.graph.NetworkBuilder
import org.bukkit.Bukkit

@Init(stage = InitStage.POST_WORLD)
object CatalogRecipeNetwork {
    private lateinit var network: Network<ItemX, CatalogRecipeEdge>

    @InitFun
    private fun init() {
        // 监听原版配方注册完成事件, 重建网络
        MapEventBus.subscribe<MinecraftRecipeRegistrationDoneEvent> { rebuildNetwork() }
    }

    private fun rebuildNetwork() {
        network = buildNetWork()
    }

    private fun buildNetWork(): Network<ItemX, CatalogRecipeEdge> {
        // 自指和平行边都是需要的, 举例说明:
        // 自指: 锻造模板复制有序合成配方(模板+钻石等->模板*2)
        // 平行边: 淡灰色染料无序合成配方(白色染料+灰色染料->淡灰色染料*2 白色染料*2+黑色染料->灰色染料*3)
        val network: MutableNetwork<ItemX, CatalogRecipeEdge> = NetworkBuilder.directed().allowsSelfLoops(true).allowsParallelEdges(true).build()

        // 原版配方
        for (bukkitRecipe in Bukkit.recipeIterator()) {
            // 为空意味着是图鉴无法显示的原版特殊配方, 直接跳过
            val catalogRecipe = convertToBukkitRecipeAdapter(bukkitRecipe) ?: continue

            // 当配方输入和输出均非空时才会添加节点和边
            // TODO 锻造台纹饰配方本身空输出, 需要添加一个占位物品作为节点
            if (catalogRecipe.getLookupInputs().isEmpty() || catalogRecipe.getLookupOutputs().isEmpty()) continue

            for (inputNode in catalogRecipe.getLookupInputs()) {
                for (outputNode in catalogRecipe.getLookupOutputs()) {
                    network.addNode(inputNode)
                    network.addNode(outputNode)
                    network.addEdge(inputNode, outputNode, CatalogRecipeEdge(catalogRecipe))
                }
            }
        }

        // TODO 工作站配方

        return network
    }

    /**
     * 获取特定物品的所有获取方式 (来源).
     */
    fun getSource(node: ItemX): Set<CatalogRecipe> {
        return network.inEdges(node).map { it.catalogRecipe }.toSet()
    }

    /**
     * 获取特定物品的所有可参与制作 (用途).
     */
    fun getUsage(node: ItemX): Set<CatalogRecipe> {
        return network.outEdges(node).map { it.catalogRecipe }.toSet()
    }

    /**
     * 封装一个 [CatalogRecipe] 作为 [Network] 的边.
     */
    class CatalogRecipeEdge(
        val catalogRecipe: CatalogRecipe,
    )

}

package cc.mewcraft.wakame.item.templates.filters

import cc.mewcraft.wakame.element.ELEMENT_EXTERNALS
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.kizami.KIZAMI_EXTERNALS
import cc.mewcraft.wakame.random3.Filter
import cc.mewcraft.wakame.random3.FilterNodeFacade
import cc.mewcraft.wakame.random3.NodeFacadeSupport
import cc.mewcraft.wakame.random3.NodeRepository
import cc.mewcraft.wakame.rarity.RARITY_EXTERNALS
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.registry.KizamiRegistry
import cc.mewcraft.wakame.registry.RarityRegistry
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.nio.file.Path
import kotlin.io.path.Path

@Init(
    stage = InitStage.PRE_WORLD,
    runBefore = [ItemRegistry::class],
    runAfter = [ElementRegistry::class, KizamiRegistry::class, RarityRegistry::class],
)
@Reload(
    runAfter = [ElementRegistry::class, KizamiRegistry::class, RarityRegistry::class],
    runBefore = [ItemRegistry::class],
)
//@PreWorldDependency(
//    runBefore = [ElementRegistry::class, KizamiRegistry::class, RarityRegistry::class],
//    runAfter = [ItemRegistry::class]
//)
//@ReloadDependency(
//    runBefore = [ElementRegistry::class, KizamiRegistry::class, RarityRegistry::class],
//    runAfter = [ItemRegistry::class]
//)
internal object ItemFilterNodeFacade : FilterNodeFacade<ItemGenerationContext>() {
    override val dataDir: Path = Path("random/items/filters")
    override val serializers: TypeSerializerCollection = TypeSerializerCollection.builder().apply {
        registerAll(get(named(ELEMENT_EXTERNALS)))
        registerAll(get(named(KIZAMI_EXTERNALS)))
        registerAll(get(named(RARITY_EXTERNALS)))
        kregister(FilterSerializer)
    }.build()

    override val repository: NodeRepository<Filter<ItemGenerationContext>> = NodeRepository()

    override fun decodeNodeData(node: ConfigurationNode): Filter<ItemGenerationContext> {
        // 实现 FilterNodeReader,
        // 这样我们才能获得泛型信息,
        // 否则 krequire 无法工作
        return node.krequire<Filter<ItemGenerationContext>>()
    }

    @InitFun
    fun onPreWorld() {
        NodeFacadeSupport.reload(this)
    }

    @ReloadFun
    private fun onReload() {
        NodeFacadeSupport.reload(this)
    }
}
package cc.mewcraft.wakame.item.templates.filters

import cc.mewcraft.wakame.element.ELEMENT_EXTERNALS
import cc.mewcraft.wakame.initializer.*
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.kizami.KIZAMI_EXTERNALS
import cc.mewcraft.wakame.random3.*
import cc.mewcraft.wakame.rarity.RARITY_EXTERNALS
import cc.mewcraft.wakame.registry.*
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.nio.file.Path

@PreWorldDependency(
    runBefore = [ElementRegistry::class, KizamiRegistry::class, RarityRegistry::class],
    runAfter = [ItemRegistry::class]
)
@ReloadDependency(
    runBefore = [ElementRegistry::class, KizamiRegistry::class, RarityRegistry::class],
    runAfter = [ItemRegistry::class]
)
internal class ItemFilterNodeFacade(
    override val dataDir: Path,
) : FilterNodeFacade<ItemGenerationContext>(), Initializable {
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

    override fun onPreWorld() {
        NodeFacadeSupport.reload(this)
    }

    override fun onReload() {
        NodeFacadeSupport.reload(this)
    }
}
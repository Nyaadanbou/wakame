package cc.mewcraft.wakame.item.templates.filters

import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.item.template.ItemGenerationContext
import cc.mewcraft.wakame.random3.Filter
import cc.mewcraft.wakame.random3.FilterNodeFacade
import cc.mewcraft.wakame.random3.NodeFacadeSupport
import cc.mewcraft.wakame.random3.NodeRepository
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.require
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.nio.file.Path
import kotlin.io.path.Path

@Init(
    stage = InitStage.PRE_WORLD,
)
@Reload
internal object ItemFilterNodeFacade : FilterNodeFacade<ItemGenerationContext>() {
    override val dataDir: Path = Path("random/items/filters")
    override val serializers: TypeSerializerCollection = TypeSerializerCollection.builder()
        .kregister(FilterSerializer)
        .build()

    @InitFun
    fun init() {
        NodeFacadeSupport.reload(this)
    }

    @ReloadFun
    fun reload() {
        NodeFacadeSupport.reload(this)
    }

    override val repository: NodeRepository<Filter<ItemGenerationContext>> = NodeRepository()

    override fun decodeNodeData(node: ConfigurationNode): Filter<ItemGenerationContext> {
        // 实现 FilterNodeReader,
        // 这样我们才能获得泛型信息,
        // 否则 krequire 无法工作
        return node.require()
    }
}
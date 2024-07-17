package cc.mewcraft.wakame.random3

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PreWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

// 文件说明:
// 包含 Filter 关于 random3.Node 的实现

// 开发日记 2024/7/16
// 一个 FilterNodeReader 不仅用来构建 SharedStorage
// 中的 random3.Node, 还要被用来构建 Pool 中的 Node.
// 这对于 SampleNodeReader 来说也是一样的.
/**
 * 读取一个 [Filter] 的 [Node].
 *
 * 你必须实现该类, 才能让泛型正确传递.
 */
// TODO FilterNodeReader 合并到 FilterNodeFacade 中
abstract class FilterNodeReader<C : SelectionContext> : NodeReader<Filter<C>>()

@PreWorldDependency(
    runAfter = [ItemRegistry::class]
)
@ReloadDependency(
    runAfter = [ItemRegistry::class]
)
// TODO 应该作为一个抽象类, 必须由需要的模块自己实现. 这样的话, 对于实现类, 泛型就可以“省略”掉了.
// TODO 给定一个 Path, 每个类负责创建它自己的 ConfigProvider, 这样可以避免在 koinModule 中定义太复杂的对象构建过程
// TODO 读取 Path 内的所有文件, 每个文件作为一个单独的 Entry.
class FilterNodeFacade<C : SelectionContext>(
    override val config: ConfigProvider,
    override val reader: FilterNodeReader<C>,
) : NodeFacade<Filter<C>>(), Initializable, KoinComponent {
    override val repository: NodeRepository<Filter<C>> = NodeRepository()
    override val typeToken: TypeToken<NodeFacade<Filter<C>>> = typeTokenOf()

    override fun onPreWorld() {
        loadConfig()
    }

    override fun onReload() {
        loadConfig()
    }

    private val logger: Logger by inject()
    private fun loadConfig() {
        logger.info("Loading: ${config.relPath}")
        populate()
    }
}
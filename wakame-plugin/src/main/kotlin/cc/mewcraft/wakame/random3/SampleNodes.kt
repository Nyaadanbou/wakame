package cc.mewcraft.wakame.random3

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PreWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.registry.ItemRegistry
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.typeTokenOf
import io.leangen.geantyref.TypeToken
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode

// 文件说明:
// 包含 Sample 关于 random3.Node 的实现

/**
 * 读取一个 [Sample] 的 [Node].
 *
 * 你必须实现该类, 才能让泛型正确传递.
 */
// 开发日记 2024/7/16
// 每种可能的 Sample 类型都应该有一个对应的 SampleNodeReader.
// 例如 TemplateCoreSampleNodeReader, ElementSampleNodeReader 等.
// TODO SampleNodeReader 合并到 SampleNodeFacade 中
abstract class SampleNodeReader<V, C : SelectionContext> : NodeReader<Sample<V, C>>() {
    // 开发日记 2024/7/16
    // 截止到现在, Sample.data 的实现都不带泛型,
    // 但是考虑到未来 random3 模块会用于其他任何地方,
    // 这里还是储存了一个 TypeToken 以备不时之需.
    /**
     * [Sample.data] 的类型.
     */
    abstract val sampleValueType: TypeToken<V>

    /**
     * [Sample] 的 [FilterNodeFacade].
     */
    abstract val filterNodeFacade: FilterNodeFacade<C>

    /**
     * 从这个 [Sample] 的 [node] 中读取其封装的数据.
     */
    abstract fun readData(node: ConfigurationNode): V

    override fun readValue(node: ConfigurationNode): Sample<V, C> {
        val data = readData(node)
        val weight = node.node("weight").krequire<Double>()
        val filters = NodeContainer(filterNodeFacade.repository()) {
            for (listChild in node.node("filters").childrenList()) {
                node(filterNodeFacade.reader().readNode(listChild))
            }
        }
        val marks = node.node("marks").string?.let { StringMark(it) }
        return Sample(data, weight, filters, marks)
    }
}

@PreWorldDependency(
    runAfter = [ItemRegistry::class]
)
@ReloadDependency(
    runAfter = [ItemRegistry::class]
)
class SampleNodeFacade<V, C : SelectionContext>(
    override val config: ConfigProvider,
    override val reader: SampleNodeReader<V, C>,
) : NodeFacade<Sample<V, C>>(), Initializable, KoinComponent {
    override val repository: NodeRepository<Sample<V, C>> = NodeRepository()
    override val typeToken: TypeToken<NodeFacade<Sample<V, C>>> = typeTokenOf()

    override fun onPreWorld() {
        loadConfig()
    }

    override fun onReload() {
        loadConfig()
    }

    private val logger: Logger by inject()
    private fun loadConfig() {
        logger.info("Loading:  ${config.relPath}")
        populate()
    }
}
package cc.mewcraft.wakame.random3

import cc.mewcraft.wakame.util.krequire
import io.leangen.geantyref.TypeToken
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.nio.file.Path

// 文件说明:
// 包含 Sample 关于 random3.Node 的实现

// 开发日记 2024/7/16
// 每种可能的 Sample 类型都应该有一个对应的 SampleNodeReader.
// 例如 TemplateCoreSampleNodeReader, ElementSampleNodeReader 等.
/**
 * 封装了类型 [Sample] 所需要的所有 [Node] 相关的实现.
 */
abstract class SampleNodeFacade<V, C : RandomSelectorContext> : NodeFacade<Sample<V, C>>() {
    /**
     * 参考 [NodeFacade.dataDir].
     */
    abstract override val dataDir: Path

    /**
     * 参考 [NodeFacade.serializers].
     */
    abstract override val serializers: TypeSerializerCollection

    /**
     * 参考 [NodeFacade.repository].
     */
    abstract override val repository: NodeRepository<Sample<V, C>>

    /**
     * [Sample] 的 [FilterNodeFacade].
     */
    abstract val filterNodeFacade: FilterNodeFacade<C>

    // 开发日记 2024/7/16
    // 截止到现在, Sample.data 的实现都不带泛型,
    // 但是考虑到未来 random3 模块会用于其他任何地方,
    // 这里还是储存了一个 TypeToken 以备不时之需.
    /**
     * [Sample.data] 的类型.
     */
    abstract val sampleDataType: TypeToken<V>

    /**
     * 从这个 [Sample] 的 [node] 中读取其封装的数据.
     */
    abstract fun decodeSampleData(node: ConfigurationNode): V

    /**
     * 定义样本的“内置过滤器”.
     *
     * “内置过滤器”可以被认为是那些会自动添加到样本中的过滤器, 而无需在配置中特别配置它们.
     *
     * @return 内在过滤器
     */
    abstract fun intrinsicFilters(value: V): Collection<Filter<C>>

    final override fun decodeNodeData(node: ConfigurationNode): Sample<V, C> {
        val data = decodeSampleData(node)
        val weight = node.node("weight").krequire<Double>()
        val filters = NodeContainer(filterNodeFacade.repository) {
            // 添加内在过滤器
            val intrinsics = intrinsicFilters(data)
            for (filter in intrinsics) {
                local(filter.kind, filter)
            }

            // 添加配置中的过滤器
            for (listChild in node.node("filters").childrenList()) {
                node(filterNodeFacade.decodeNode(listChild))
            }
        }
        val marks = node.node("marks").string?.let { Mark(it) }
        return Sample(data, weight, filters, marks)
    }
}
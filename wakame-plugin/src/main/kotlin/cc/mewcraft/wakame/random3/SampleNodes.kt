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
abstract class SampleNodeFacade<V, C : SelectionContext> : NodeFacade<Sample<V, C>>() {
    // Abstracts

    abstract override val dataDir: Path
    abstract override val serializers: TypeSerializerCollection
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

    // Overrides: NodeFacade

    override fun decodeNodeData(node: ConfigurationNode): Sample<V, C> {
        val data = decodeSampleData(node)
        val weight = node.node("weight").krequire<Double>()
        val filters = NodeContainer(filterNodeFacade.repository) {
            for (listChild in node.node("filters").childrenList()) {
                node(filterNodeFacade.decodeNode(listChild))
            }
        }
        val marks = node.node("marks").string?.let { StringMark(it) }
        return Sample(data, weight, filters, marks)
    }
}
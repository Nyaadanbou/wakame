package cc.mewcraft.wakame.random3

import cc.mewcraft.wakame.config.ConfigProvider
import io.leangen.geantyref.TypeToken

/**
 * 包含了一个类型 [T] 所需要的所有 [Node] 相关的实现.
 */
abstract class NodeFacade<T> {
    /**
     * 本实例的类型.
     */
    abstract val typeToken: TypeToken<NodeFacade<T>>

    /**
     * 根节点必须是是一个键为 `entries`, 值为 Map 的节点.
     *
     * Map 的键是一个字符串, 值是一个 List, 其中的每一项都是一个 [LocalNode] 或 [CompositeNode].
     */
    protected abstract val config: ConfigProvider

    /**
     * 用于从 config.Node 读取 [Node] 实例.
     */
    protected abstract val reader: NodeReader<T>

    /**
     * 储存共享的 [Node<T>][Node].
     */
    protected abstract val repository: NodeRepository<T>

    fun reader(): NodeReader<T> {
        return reader
    }

    fun repository(): NodeRepository<T> {
        return repository
    }

    /**
     * 从配置文件读取所有的过滤器.
     */
    fun populate() {
        // 获取根配置
        val root = config.get().node("entries")

        // 清空 storage
        repository.clear()

        // 填充 storage
        root.childrenMap()
            .mapKeys { (key, _) -> key.toString() }
            .forEach { (entryId, mapChild) ->
                // mapChild 装的是一个 List, 其中的每一项都是一个 LocalNode 或 CompositeNode
                repository.addEntry(entryId) {
                    mapChild.childrenList().map { reader.readNode(it) }.forEach { node(it) }
                }
            }
    }
}
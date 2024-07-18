package cc.mewcraft.wakame.random3

import net.kyori.adventure.key.Key

/**
 * 一个持有 [Node] 的仓库, 用于存放共享的 [Node] 实例.
 */
interface NodeRepository<T> {
    companion object {
        /**
         * 获取一个*不可变*的空 [NodeRepository].
         */
        fun <T> empty(): NodeRepository<T> {
            return NodeRepositoryEmpty as NodeRepository<T>
        }
    }

    fun hasEntry(entryId: String): Boolean
    fun addEntry(ref: String, init: EntryBuilder<T>.() -> Unit = {})
    fun getNodes(ref: String): List<Node<T>>
    fun clear()

    /**
     * 可供外部引用的共享节点. 每个 [NodeRepository] 都包含有多个 [Entry].
     */
    interface Entry<T> {
        /**
         * 引用.
         */
        val ref: String

        /**
         * 节点.
         */
        val nodes: List<Node<T>>
    }

    /**
     * 设计上, 一个引用包含若干 [Node].
     */
    @NodeDsl
    interface EntryBuilder<T> {
        /**
         * 添加一个预先构建好的 [Node].
         */
        fun node(node: Node<T>)

        /**
         * 构建一个 [LocalNode] 并添加进引用.
         */
        fun local(key: Key, value: T)

        /**
         * 构建一个 [CompositeNode] 并添加进引用.
         */
        fun composite(key: Key, init: CompositeNode<T>.() -> Unit = {})
    }
}

fun <T> NodeRepository(
    block: NodeRepository<T>.() -> Unit = {},
): NodeRepository<T> {
    val storage = NodeRepositoryImpl<T>()
    storage.apply(block)
    return storage
}

private object NodeRepositoryEmpty : NodeRepository<Nothing> {
    override fun hasEntry(entryId: String): Boolean = false
    override fun addEntry(ref: String, init: NodeRepository.EntryBuilder<Nothing>.() -> Unit) = Unit
    override fun getNodes(ref: String): List<Node<Nothing>> = emptyList()
    override fun clear() = Unit
}

private class NodeRepositoryImpl<T> : NodeRepository<T> {
    val entries: HashMap<String, NodeRepository.Entry<T>> = HashMap()

    override fun hasEntry(entryId: String): Boolean {
        return entries.containsKey(entryId)
    }

    override fun addEntry(ref: String, init: NodeRepository.EntryBuilder<T>.() -> Unit) {
        val builder = EntryBuilderImpl<T>().apply(init)
        entries[ref] = EntryImpl(ref, builder.nodes)
    }

    override fun getNodes(ref: String): List<Node<T>> {
        val resolvedNodes = mutableListOf<Node<T>>()
        val visited = mutableSetOf<String>()
        resolveNode(ref, resolvedNodes, visited)
        return resolvedNodes
    }

    override fun clear() {
        entries.clear()
    }

    fun resolveNode(key: String, resolvedNodes: MutableList<Node<T>>, visitedKeys: MutableSet<String>) {
        if (key in visitedKeys) {
            return
        }
        visitedKeys.add(key)

        val entry = entries[key] ?: return
        for (node in entry.nodes) {
            when (node) {
                is LocalNode -> {
                    resolvedNodes.add(node)
                }

                is CompositeNode -> {
                    if (!visitedKeys.contains(node.key.value())) {
                        resolveNode(node.key.value(), resolvedNodes, visitedKeys)
                    }
                    for (childNode in node.getNodes()) {
                        when (childNode) {
                            is LocalNode -> resolvedNodes.add(childNode)
                            is CompositeNode -> resolveNode(childNode.key.value(), resolvedNodes, visitedKeys)
                        }
                    }
                }
            }
        }
    }

    data class EntryImpl<T>(
        override val ref: String,
        override val nodes: List<Node<T>>,
    ) : NodeRepository.Entry<T>

    data class EntryBuilderImpl<T>(
        val nodes: MutableList<Node<T>> = mutableListOf(),
    ) : NodeRepository.EntryBuilder<T> {
        override fun node(node: Node<T>) {
            nodes.add(node)
        }

        override fun local(key: Key, value: T) {
            nodes.add(LocalNode(key, value))
        }

        override fun composite(key: Key, init: CompositeNode<T>.() -> Unit) {
            require(key.namespace() == Node.NAMESPACE_GLOBAL) {
                "CompositeNode key must be in the 'global' namespace"
            }
            val compositeNode = CompositeNode<T>(key, mutableListOf())
            compositeNode.apply(init)
            nodes.add(compositeNode)
        }
    }
}
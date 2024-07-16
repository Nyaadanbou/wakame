package cc.mewcraft.wakame.random3

import cc.mewcraft.wakame.random3.NodeRepository.EntryBuilder
import net.kyori.adventure.key.Key

/**
 * 代表一个节点.
 *
 * 节点可以自身储存任意值, 也可以引用一个全局的节点.
 * 当节点引用一个全局节点时, 支持递归解析全局节点的值.
 */
sealed interface Node<T> {
    val key: Key

    companion object {
        const val NAMESPACE_GLOBAL = "global"
    }
}

data class LocalNode<T>(
    override val key: Key,
    val value: T,
) : Node<T>

@NodeDsl
data class CompositeNode<T>(
    override val key: Key,
    private val nodes: MutableList<Node<T>>,
) : Node<T> {
    fun addNode(init: NodeBuilder<T>.() -> Unit): CompositeNode<T> {
        val builder = NodeBuilder<T>().apply(init)
        nodes.addAll(builder.nodes)
        return this
    }

    fun getNodes(): List<Node<T>> {
        return nodes
    }

    @NodeDsl
    class NodeBuilder<T> {
        val nodes = mutableListOf<Node<T>>()

        fun node(node: Node<T>) {
            nodes.add(node)
        }

        fun local(key: Key, value: T) {
            nodes.add(LocalNode(key, value))
        }

        fun composite(key: Key, init: NodeBuilder<T>.() -> Unit = {}) {
            require(key.namespace() == Node.NAMESPACE_GLOBAL) {
                "CompositeNode key must be in the 'global' namespace"
            }
            val node = CompositeNode<T>(key, mutableListOf())
            node.addNode(init)
            nodes.add(node)
        }
    }
}

@NodeDsl
fun <T> NodeContainer(
    shared: NodeRepository<T>,
    block: CompositeNode.NodeBuilder<T>.() -> Unit = {},
): NodeContainer<T> {
    return NodeContainerImpl(shared).apply { set(block) }
}

/**
 * 一个持有 [Node] 的容器, 用于存放 [Node] 实例.
 */
@NodeDsl
interface NodeContainer<T> : Iterable<T> {
    companion object {
        /**
         * 获取一个*不可变*的空 [NodeContainer].
         */
        fun <T> empty(): NodeContainer<T> {
            return NodeContainerEmpty as NodeContainer<T>
        }
    }

    fun set(init: CompositeNode.NodeBuilder<T>.() -> Unit)
    fun add(init: CompositeNode.NodeBuilder<T>.() -> Unit)
    fun values(): List<T>
}

private object NodeContainerEmpty : NodeContainer<Nothing> {
    override fun set(init: CompositeNode.NodeBuilder<Nothing>.() -> Unit) = Unit
    override fun add(init: CompositeNode.NodeBuilder<Nothing>.() -> Unit) = Unit
    override fun values(): List<Nothing> = emptyList()
    override fun iterator(): Iterator<Nothing> = values().iterator()
}

@NodeDsl
private class NodeContainerImpl<T>(
    private val shared: NodeRepository<T>,
) : NodeContainer<T> {
    companion object {
        private val ROOT_KEY = Key.key("internal:root")
    }

    private var root: CompositeNode<T>? = null

    override fun set(init: CompositeNode.NodeBuilder<T>.() -> Unit) {
        val builder = CompositeNode.NodeBuilder<T>().apply(init)
        root = CompositeNode(ROOT_KEY, builder.nodes)
    }

    override fun add(init: CompositeNode.NodeBuilder<T>.() -> Unit) {
        val builder = CompositeNode.NodeBuilder<T>().apply(init)
        when (root) {
            null -> {
                root = CompositeNode(ROOT_KEY, builder.nodes)
            }

            is CompositeNode -> {
                (root as CompositeNode<T>).addNode(init)
            }

            else -> {
                throw IllegalStateException("Root node is not a CompositeNode")
            }
        }
    }

    override fun values(): List<T> {
        return this.toList()
    }

    override fun iterator(): Iterator<T> {
        return NodeIterator(root, shared)
    }

    class NodeIterator<T>(
        root: Node<T>?,
        private val shared: NodeRepository<T>,
    ) : Iterator<T> {
        private val valuesQueue = mutableListOf<T>()
        private val visitedGlobals = mutableSetOf<String>()

        init {
            root?.let { resolveNode(it) }
        }

        private fun resolveNode(node: Node<T>) {
            when (node) {
                is LocalNode -> {
                    // 添加值到 valuesQueue 时,
                    // 不检查值或 Node 的唯一性
                    valuesQueue.add(node.value)
                }

                is CompositeNode -> {
                    if (node.key.namespace() == Node.NAMESPACE_GLOBAL && visitedGlobals.add(node.key.value())) {
                        for (it in shared.getNodes(node.key.value())) {
                            resolveNode(it)
                        }
                    } else {
                        for (childNode in node.getNodes()) {
                            resolveNode(childNode)
                        }
                    }
                }
            }
        }

        override fun hasNext(): Boolean {
            return valuesQueue.isNotEmpty()
        }

        override fun next(): T {
            if (!hasNext()) {
                throw NoSuchElementException()
            }
            return valuesQueue.removeAt(0)
        }
    }
}

@NodeDsl
fun <T> NodeRepository(
    block: NodeRepository<T>.() -> Unit = {},
): NodeRepository<T> {
    val storage = NodeRepositoryImpl<T>()
    storage.apply(block)
    return storage
}

/**
 * 一个持有 [Node] 的仓库, 用于存放共享的 [Node] 实例.
 */
@NodeDsl
interface NodeRepository<T> {
    companion object {
        /**
         * 获取一个*不可变*的空ß [NodeRepository].
         */
        fun <T> empty(): NodeRepository<T> {
            return NodeRepositoryEmpty as NodeRepository<T>
        }
    }

    fun addEntry(ref: String, init: EntryBuilder<T>.() -> Unit = {})
    fun getNodes(ref: String): List<Node<T>>
    fun clear()

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

private object NodeRepositoryEmpty : NodeRepository<Nothing> {
    override fun addEntry(ref: String, init: EntryBuilder<Nothing>.() -> Unit) = Unit
    override fun getNodes(ref: String): List<Node<Nothing>> = emptyList()
    override fun clear() = Unit
}

private class NodeRepositoryImpl<T> : NodeRepository<T> {
    private val entries: HashMap<String, List<Node<T>>> = HashMap()

    override fun addEntry(ref: String, init: EntryBuilder<T>.() -> Unit) {
        val builder = EntryBuilderImpl<T>().apply(init)
        entries[ref] = builder.nodes
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

    private fun resolveNode(key: String, resolvedNodes: MutableList<Node<T>>, visitedKeys: MutableSet<String>) {
        if (key in visitedKeys) {
            return
        }
        visitedKeys.add(key)

        val nodes = entries[key] ?: return
        for (node in nodes) {
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

    class EntryBuilderImpl<T> : EntryBuilder<T> {
        val nodes = mutableListOf<Node<T>>()

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

@DslMarker
private annotation class NodeDsl
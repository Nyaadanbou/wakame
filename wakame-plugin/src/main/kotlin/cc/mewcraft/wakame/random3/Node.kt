package cc.mewcraft.wakame.random3

import cc.mewcraft.wakame.random3.SharedStorage.EntryBuilder
import net.kyori.adventure.key.Key

/**
 * 代表一个节点.
 *
 * 节点可以自身储存任意值, 也可以引用一个全局的节点.
 * 当节点引用一个全局节点时, 会递归解析全局节点的值.
 */
sealed interface Node<T> {
    val key: Key
}

data class LocalNode<T>(
    override val key: Key, val value: T,
) : Node<T>

@NodeDsl
data class CompositeNode<T>(
    override val key: Key, private val nodes: MutableList<Node<T>> = mutableListOf(),
) : Node<T> {
    fun addNode(init: Builder<T>.() -> Unit): CompositeNode<T> {
        val builder = Builder<T>().apply(init)
        nodes.addAll(builder.nodes)
        return this
    }

    fun getNodes(): List<Node<T>> {
        return nodes
    }

    @NodeDsl
    class Builder<T> {
        val nodes = mutableListOf<Node<T>>()

        fun local(key: String, value: T) {
            nodes.add(LocalNode(Key.key(key), value))
        }

        fun composite(key: String, init: Builder<T>.() -> Unit = {}) {
            val node = CompositeNode<T>(Key.key(key))
            node.addNode(init)
            nodes.add(node)
        }
    }
}

@NodeDsl
fun <T> NodeContainer(
    shared: SharedStorage<T>,
    block: CompositeNode.Builder<T>.() -> Unit = {},
): NodeContainer<T> {
    return NodeContainerImpl(shared).apply { set(block) }
}

@NodeDsl
interface NodeContainer<T> {
    companion object {
        /**
         * 获取一个*不可变*的空 [NodeContainer].
         */
        fun <T> empty(): NodeContainer<T> {
            return NodeContainerEmpty as NodeContainer<T>
        }
    }

    fun set(init: CompositeNode.Builder<T>.() -> Unit)
    fun add(init: CompositeNode.Builder<T>.() -> Unit)
    fun values(): List<T>
    fun iterator(): Iterator<T>
}

private object NodeContainerEmpty : NodeContainer<Nothing> {
    override fun set(init: CompositeNode.Builder<Nothing>.() -> Unit) = Unit
    override fun add(init: CompositeNode.Builder<Nothing>.() -> Unit) = Unit
    override fun values(): List<Nothing> = emptyList()
    override fun iterator(): Iterator<Nothing> = values().iterator()
}

@NodeDsl
private class NodeContainerImpl<T>(
    private val shared: SharedStorage<T>,
) : Iterable<T>, NodeContainer<T> {
    private var root: Node<T>? = null

    override fun set(init: CompositeNode.Builder<T>.() -> Unit) {
        val builder = CompositeNode.Builder<T>().apply(init)
        root = CompositeNode(Key.key("root"), builder.nodes)
    }

    override fun add(init: CompositeNode.Builder<T>.() -> Unit) {
        val builder = CompositeNode.Builder<T>().apply(init)
        when (root) {
            null -> {
                root = CompositeNode(Key.key("root"), builder.nodes)
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
        private val shared: SharedStorage<T>,
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
                    if (node.key.namespace() == SharedStorage.NAMESPACE_GLOBAL && visitedGlobals.add(node.key.value())) {
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
fun <T> SharedStorage(
    block: SharedStorage<T>.() -> Unit = {},
): SharedStorage<T> {
    val storage = SharedStorageImpl<T>()
    storage.apply(block)
    return storage
}

@NodeDsl
interface SharedStorage<T> {
    companion object {
        const val NAMESPACE_GLOBAL = "global"

        /**
         * 获取一个*不可变*的空ß [SharedStorage].
         */
        fun <T> empty(): SharedStorage<T> {
            return SharedStorageEmpty as SharedStorage<T>
        }
    }

    fun addEntry(key: String, init: EntryBuilder<T>.() -> Unit = {})
    fun getNodes(key: String): List<Node<T>>

    @NodeDsl
    interface EntryBuilder<T> {
        fun local(key: String, value: T)
        fun composite(key: String, init: CompositeNode<T>.() -> Unit = {})
    }
}

object SharedStorageEmpty : SharedStorage<Nothing> {
    override fun addEntry(key: String, init: EntryBuilder<Nothing>.() -> Unit) = Unit
    override fun getNodes(key: String): List<Node<Nothing>> = emptyList()
}

private class SharedStorageImpl<T> : SharedStorage<T> {
    private val entries: HashMap<String, List<Node<T>>> = HashMap()

    override fun addEntry(key: String, init: EntryBuilder<T>.() -> Unit) {
        val builder = EntryBuilderImpl<T>().apply(init)
        entries[key] = builder.nodes
    }

    override fun getNodes(key: String): List<Node<T>> {
        val resolvedNodes = mutableListOf<Node<T>>()
        val visited = mutableSetOf<String>()
        resolveNode(key, resolvedNodes, visited)
        return resolvedNodes
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

        override fun local(key: String, value: T) {
            nodes.add(LocalNode(Key.key(key), value))
        }

        override fun composite(key: String, init: CompositeNode<T>.() -> Unit) {
            val compositeNode = CompositeNode<T>(Key.key(key)).apply(init)
            nodes.add(compositeNode)
        }
    }
}

@DslMarker
annotation class NodeDsl
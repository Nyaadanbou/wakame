package random2

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

    class Builder<T> {
        val nodes = mutableListOf<Node<T>>()

        fun node(key: String, value: T) {
            nodes.add(LocalNode(Key.key(key), value))
        }

        fun compositeNode(key: String, init: Builder<T>.() -> Unit = {}) {
            val compositeNode = CompositeNode<T>(Key.key(key))
            compositeNode.addNode(init)
            nodes.add(compositeNode)
        }
    }
}

class NodeContainer<T>(
    private val shared: SharedStorage<T>,
) : Iterable<T> {
    var root: Node<T>? = null

    fun values(): List<T> {
        return this.toList()
    }

    override fun iterator(): Iterator<T> {
        return NodeIterator(root, shared)
    }

    private class NodeIterator<T>(
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
                        for (it in shared.getNodesByKey(node.key.value())) {
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

class SharedStorage<T> {
    companion object {
        const val NAMESPACE_GLOBAL = "global"
    }

    private val entries: HashMap<String, List<Node<T>>> = HashMap()

    fun addEntry(key: String, init: Builder<T>.() -> Unit = {}) {
        val builder = Builder<T>().apply(init)
        entries[key] = builder.nodes
    }

    fun getNodesByKey(key: String): List<Node<T>> {
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

    class Builder<T> {
        val nodes = mutableListOf<Node<T>>()

        fun node(key: String, value: T) {
            nodes.add(LocalNode(Key.key(key), value))
        }

        fun compositeNode(key: String, init: CompositeNode<T>.() -> Unit = {}) {
            val compositeNode = CompositeNode<T>(Key.key(key)).apply(init)
            nodes.add(compositeNode)
        }
    }
}
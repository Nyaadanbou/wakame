package random2

import net.kyori.adventure.key.Key

sealed class Node<T>(open val key: Key)

data class LocalNode<T>(
    override val key: Key,
    val value: T
) : Node<T>(key)

data class CompositeNode<T>(
    override val key: Key,
    private val nodes: MutableList<Node<T>> = mutableListOf()
) : Node<T>(key) {
    fun addNode(node: Node<T>): CompositeNode<T> {
        nodes.add(node)
        return this
    }

    fun getNodes(): List<Node<T>> = nodes
}

class NodeContainer<T>(private val globalDataSource: GlobalDataSource<T>) : Iterable<T> {
    var root: Node<T>? = null

    fun values(): List<T> = this.toList()

    override fun iterator(): Iterator<T> = NodeIterator(root, globalDataSource)

    private class NodeIterator<T>(
        root: Node<T>?,
        private val globalDataSource: GlobalDataSource<T>
    ) : Iterator<T> {
        private val valuesQueue = mutableListOf<T>()
        private val visitedGlobals = mutableSetOf<String>()

        init {
            root?.let { resolveNode(it) }
        }

        private fun resolveNode(node: Node<T>) {
            when (node) {
                is LocalNode -> valuesQueue.add(node.value)
                is CompositeNode -> {
                    if (node.key.namespace() == "global" && visitedGlobals.add(node.key.value())) {
                        globalDataSource.getNodesByKey(node.key.value()).forEach { resolveNode(it) }
                    } else {
                        node.getNodes().forEach { childNode ->
                            resolveNode(childNode)
                        }
                    }
                }
            }
        }

        override fun hasNext(): Boolean = valuesQueue.isNotEmpty()

        override fun next(): T {
            if (!hasNext()) throw NoSuchElementException()
            return valuesQueue.removeAt(0)
        }
    }
}

class GlobalDataSource<T> {
    private val entries: HashMap<String, List<Node<T>>> = HashMap()

    fun addEntry(key: String, nodes: List<Node<T>>) {
        entries[key] = nodes
    }

    fun getNodesByKey(key: String): List<Node<T>> {
        val resolvedNodes = mutableListOf<Node<T>>()
        val visited = mutableSetOf<String>()
        resolveNode(key, resolvedNodes, visited)
        return resolvedNodes
    }

    private fun resolveNode(key: String, resolvedNodes: MutableList<Node<T>>, visited: MutableSet<String>) {
        if (key in visited) return
        visited.add(key)

        val nodes = entries[key] ?: return
        nodes.forEach { node ->
            when (node) {
                is LocalNode -> resolvedNodes.add(node)
                is CompositeNode -> {
                    if (!visited.contains(node.key.value())) {
                        resolveNode(node.key.value(), resolvedNodes, visited)
                    }
                    node.getNodes().forEach { childNode ->
                        when (childNode) {
                            is LocalNode -> resolvedNodes.add(childNode)
                            is CompositeNode -> resolveNode(childNode.key.value(), resolvedNodes, visited)
                        }
                    }
                }
            }
        }
    }
}
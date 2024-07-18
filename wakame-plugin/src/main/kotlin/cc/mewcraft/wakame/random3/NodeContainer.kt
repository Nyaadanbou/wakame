package cc.mewcraft.wakame.random3

import net.kyori.adventure.key.Key

/**
 * 一个持有 [Node] 的容器, 用于存放 [Node] 实例.
 */
interface NodeContainer<T> : Iterable<T> {
    companion object {
        /**
         * 获取一个*不可变*的空 [NodeContainer].
         */
        fun <T> empty(): NodeContainer<T> {
            return NodeContainerEmpty as NodeContainer<T>
        }
    }

    fun set(init: NodeBuilder<T>.() -> Unit)
    fun add(init: NodeBuilder<T>.() -> Unit)
    fun values(): List<T>
}

fun <T> NodeContainer(
    shared: NodeRepository<T>,
    block: NodeBuilder<T>.() -> Unit = {},
): NodeContainer<T> {
    return NodeContainerImpl(shared).apply { set(block) }
}

private object NodeContainerEmpty : NodeContainer<Nothing> {
    override fun set(init: NodeBuilder<Nothing>.() -> Unit) = Unit
    override fun add(init: NodeBuilder<Nothing>.() -> Unit) = Unit
    override fun values(): List<Nothing> = emptyList()
    override fun iterator(): Iterator<Nothing> = values().iterator()
}

private class NodeContainerImpl<T>(
    private val shared: NodeRepository<T>,
) : NodeContainer<T> {
    companion object {
        private val ROOT_KEY = Key.key("internal:root")
    }

    private var root: CompositeNode<T>? = null

    override fun set(init: NodeBuilder<T>.() -> Unit) {
        val builder = NodeBuilder<T>().apply(init)
        root = CompositeNode(ROOT_KEY, builder.nodes)
    }

    override fun add(init: NodeBuilder<T>.() -> Unit) {
        val builder = NodeBuilder<T>().apply(init)
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
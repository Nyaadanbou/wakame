package cc.mewcraft.wakame.random3

import net.kyori.adventure.key.Key

@DslMarker
internal annotation class NodeDsl

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

data class LocalNode<T>(
    override val key: Key,
    val value: T,
) : Node<T>

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
}
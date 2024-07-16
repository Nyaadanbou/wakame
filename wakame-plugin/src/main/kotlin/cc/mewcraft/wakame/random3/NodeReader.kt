package cc.mewcraft.wakame.random3

import cc.mewcraft.wakame.util.javaTypeOf
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException

/**
 * 封装了从一个 [ConfigurationNode] 读取一个 [Node] 的通用逻辑.
 */
abstract class NodeReader<T> {
    /**
     * 将一个 [ConfigurationNode] 读成 [T].
     *
     * ## Node structure
     * ```yaml
     * <node>:
     *   # 下面只是随便写点数据演示.
     *   # 具体结构取决于泛型 [T].
     *   foo: bar
     *   bar: foo
     * ```
     */
    protected abstract fun readValue(node: ConfigurationNode): T

    /**
     * 从 [ConfigurationNode] 中读取 "type" (类型为 [Key]).
     *
     * "type" 的命名空间将决定 [Node] 的解析结果 (参考 [Node] 的实现).
     */
    protected fun ConfigurationNode.type(): Key {
        val rawType = this.node("type").string ?: throw SerializationException(
            this, javaTypeOf<String>(), "The 'type' of this node is not specified"
        )
        val type = runCatching { Key.key(rawType) }.getOrElse {
            throw SerializationException(this, javaTypeOf<Key>(), it)
        }
        return type
    }

    /**
     * 将一个 [ConfigurationNode] 读成 [Node<T>][Node].
     *
     * ## Node structure
     * ```yaml
     * <node>:
     *   # type 是必须要有的, 类型为 Key
     *   type: <key>
     *   # 其余都是根据泛型 [T] 来决定的,
     *   # 这些会由 readValue() 来处理.
     *   foo: bar
     *   bar: foo
     * ```
     */
    fun readNode(node: ConfigurationNode): Node<T> {
        val type = node.type()

        // 首先看看是不是 CompositeNode; 如果是的话就直接构建一个 CompositeNode, 然后返回.
        if (type.namespace() == Node.NAMESPACE_GLOBAL) {
            // 这里只需要直接构建一个 CompositeNode, 并不需要去读取 CompositeNode 所指向的数据.
            // 这是因为 CompositeNode 的数据是懒加载的 - 只有需要的时候才会从 SharedStorage 获取.
            return CompositeNode(type, mutableListOf())
        }

        // 否则就是 LocalNode
        val value = readValue(node)
        return LocalNode(type, value)
    }
}
package cc.mewcraft.wakame.random3

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.util.javaTypeOf
import cc.mewcraft.wakame.util.typeTokenOf
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.RepresentationHint
import org.spongepowered.configurate.kotlin.extensions.contains
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.Collections

/**
 * [Group] 是一个包含了若干 [Pool] 的集合
 *
 * 同时, 该接口提供函数 [select], 让你从其中包含的所有 [Pool]
 * 中随机选择一个 [S]. 关于具体的选择过程, 见函数 [select] 的说明.
 *
 * @param S 样本所携带的实例
 * @param C 条件所需要的上下文
 */
interface Group<S, C : RandomSelectorContext> {

    /**
     * 包含一些 [Group] 的构造方法.
     */
    companion object Factory {
        fun <S, C : RandomSelectorContext> empty(): Group<S, C> {
            return GroupEmpty as Group<S, C>
        }
    }

    /**
     * 该 [Group] 包含的所有 [Pool].
     *
     * **实现上该 [Map] 的遍历顺序是固定的.**
     */
    val pools: Map<String, Pool<S, C>>

    /**
     * 进入该 [Group] 需要满足的全部条件.
     */
    val filters: NodeContainer<Filter<C>>

    /**
     * The fallback [Pool] when nothing is picked from [pools].
     */
    val default: Pool<S, C>

    /**
     * Randomly pick several [S] with given [context].
     *
     * Returns an empty list if:
     * - the [filters] are not met, and
     * - [default] returns a `null`.
     *
     * ## 随机抽取的步骤
     * 1. 首先检查 `this` 的 [filters]
     *    * 如果条件满足, 则继续接下来的操作
     *    * 如果条件不满足, 则直接返回空列表
     * 2. 按顺序从 `this` 的 [pools] 选择第一个满足条件的池, 然后调用 [Pool.select] 来选择最终的样本
     *    * 如果 [pools] 中没有满足条件的, 将从 [default] 中选择一个结果
     *    * 如果 [default] 也没有结果, 最终将返回空列表
     */
    fun select(context: C): List<S>
}

interface GroupBuilder<S, C : RandomSelectorContext> {
    val pools: MutableMap<String, Pool<S, C>>
    val filters: NodeContainer<Filter<C>>
    var default: Pool<S, C>
}

/**
 * Shares the common code between pool serializers.
 *
 * The subclasses must assume that the config structure is the following:
 *
 * ## Node structure 1
 *
 * This structure can have all the components specified.
 *
 * ```yaml
 * <node>:
 *   filters: <children list>
 *   selects: <children map>
 *   default: <pool>
 * ```
 *
 * ## Node structure 2
 *
 * This structure only has the `selects` component.
 * This could be useful if neither `filters` nor `default` are needed.
 *
 * ```yaml
 * <node>:
 *   <children map>
 * ```
 *
 * @param V the type of content
 * @param C the type of context
 */
abstract class GroupSerializer<V, C : RandomSelectorContext> : SchemaSerializer<Group<V, C>> {
    companion object Constants {
        val HINT_NODE_SHARED_POOLS: RepresentationHint<ConfigurationNode> = RepresentationHint.of("node_shared_pools", typeTokenOf<ConfigurationNode>())
        private const val PATH_FILTERS = "filters"
        private const val PATH_SELECTS = "selects"
        private const val PATH_DEFAULT = "default"
    }

    protected abstract val filterNodeFacade: FilterNodeFacade<C>
    protected abstract fun poolConstructor(node: ConfigurationNode): Pool<V, C>
    // protected abstract fun filterConstructor(node: ConfigurationNode): Filter<C>

    final override fun deserialize(type: Type, node: ConfigurationNode): Group<V, C> {
        when {
            // Node structure 1:
            // 这是一个纯粹的池映射, 没有过滤器, 也没有默认值
            node.isMap && !node.contains(PATH_FILTERS) && !node.contains(PATH_SELECTS) && !node.contains(PATH_DEFAULT) -> {
                val pools = decodeAsPoolMap(node, node)
                val group = GroupImpl(
                    pools = pools,
                    filters = NodeContainer.empty(),
                    default = Pool.empty()
                )
                return group
            }

            // Node structure 2:
            // 这是一个完整的结构, 可能包含所有组件
            node.isMap -> {
                val pools = decodeAsPoolMap(node, node.node(PATH_SELECTS))
                val filters = NodeContainer(filterNodeFacade.repository) {
                    for (listChild in node.node(PATH_FILTERS).childrenList()) {
                        node(filterNodeFacade.decodeNode(listChild))
                    }
                }
                val default = node.node(PATH_DEFAULT).let {
                    if (it.virtual()) {
                        Pool.empty()
                    } else {
                        poolConstructor(node.node(PATH_DEFAULT))
                    }
                }
                val group = GroupImpl(
                    pools = pools,
                    filters = filters,
                    default = default
                )
                return group
            }

            // Unknown structure
            // 这是一个未知的格式
            else -> {
                throw SerializationException(node.path(), type, "Failed to deserialize ${type}. Check your config.")
            }
        }
    }

    /**
     * ## [groupNode] structure
     *
     * ## [selectNode] structure
     *
     */
    private fun decodeAsPoolMap(groupNode: ConfigurationNode, selectNode: ConfigurationNode): Map<String, Pool<V, C>> {
        val ret = mutableMapOf<String, Pool<V, C>>()
        selectNode.childrenMap()
            .mapKeys { it.key.toString() }
            .forEach { (poolName, localPoolNode) ->
                val rawScalar = localPoolNode.rawScalar()
                if (rawScalar != null) {
                    // 这是一个原始字符串,
                    // 意味着它引用了共享池中的一个 config.Node,
                    // 因此, 我们需要将外部的节点传递给工厂函数.
                    val sharedPoolsNode = groupNode.hint(HINT_NODE_SHARED_POOLS) ?: throw SerializationException(
                        selectNode, javaTypeOf<Group<V, C>>(), "Can't find hint '${HINT_NODE_SHARED_POOLS.identifier()}'"
                    )
                    val externalPoolNode = sharedPoolsNode.node(rawScalar)
                    ret.put(poolName, poolConstructor(externalPoolNode))
                } else {
                    // 这不是一个原始字符串 - 我们只传递本地节点
                    ret.put(poolName, poolConstructor(localPoolNode))
                }
            }
        return ret
    }
}


/* Implementations */


private object GroupEmpty : Group<Nothing, RandomSelectorContext> {
    override val pools: Map<String, Pool<Nothing, RandomSelectorContext>> = emptyMap()
    override val filters: NodeContainer<Filter<RandomSelectorContext>> = NodeContainer.empty()
    override val default: Pool<Nothing, RandomSelectorContext> = Pool.empty()
    override fun select(context: RandomSelectorContext): List<Nothing> = Collections.emptyList()
}

private class GroupImpl<S, C : RandomSelectorContext>(
    override val pools: Map<String, Pool<S, C>>,
    override val filters: NodeContainer<Filter<C>>,
    override val default: Pool<S, C>,
) : Group<S, C> {
    override fun select(context: C): List<S> {
        val test = filters.all {
            it.test(context)
        }
        if (!test) {
            // 如果该 Group 本身的条件没有全部满足, 则直接返回空列表
            return emptyList()
        }

        val pool = pools.values.firstOrNull { pool ->
            // 按顺序找到一个符合条件的 pool
            pool.filters.all {
                it.test(context)
            }
        }

        if (pool != null) {
            // 我们找到了一个满足条件的 pool, 因此从这个 pool 中选择
            return pool.select(context)
        }

        // pools 中没有一个满足条件的, 因此从 fallback 中选择
        return default.select(context)
    }
}

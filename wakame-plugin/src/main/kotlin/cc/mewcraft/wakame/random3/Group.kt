package cc.mewcraft.wakame.random3

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.util.javaTypeOf
import cc.mewcraft.wakame.util.typeTokenOf
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.RepresentationHint
import org.spongepowered.configurate.kotlin.extensions.contains
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.Collections

/**
 * [Group] 是一个包含了若干 [Pool] 的集合
 *
 * 同时, 该接口提供函数 [select], 让你从其中包含的所有 [pools][Pool]
 * 中随机选择一个 [S]. 关于具体的选择过程, 见函数 [select] 的说明.
 *
 * @param S 样本所携带的实例
 * @param C 条件所需要的上下文
 */
interface Group<S, C : SelectionContext> {

    /**
     * 该 [group][Group] 包含的所有 [pools][Pool].
     */
    val pools: Map<String, Pool<S, C>>

    /**
     * 进入该 [group][Group] 需要满足的条件. 多个条件为 `AND` 关系.
     */
    val filters: NodeContainer<Filter<C>>

    /**
     * The fallback [pool][Pool] when nothing is picked from [pools].
     */
    val default: Pool<S, C>

    /**
     * Randomly pick several [S's][S] with given [context].
     *
     * Returns an empty list if:
     * - [filters][filters] are not met, and
     * - [fallback pool][default] returns a `null`.
     *
     * ## 随机抽取的步骤
     * 1. 首先检查 `this` 的 [filters][filters]
     *    * 如果条件满足, 则继续接下来的操作
     *    * 如果条件不满足, 则直接返回空列表
     * 2. 按顺序从 `this` 的 [pools] 选择第一个满足条件的池, 然后调用 [Pool.pickBulk] 来选择最终的样本
     *    * 如果 [pools][Pool] 中没有满足条件的, 将从 [fallback pool][default] 中选择一个结果
     *    * 如果 [fallback pool][default] 也没有结果, 最终将返回空列表
     */
    fun select(context: C): List<S>

    companion object Factory {
        fun <S, C : SelectionContext> empty(): Group<S, C> {
            @Suppress("UNCHECKED_CAST")
            return (GroupEmpty as Group<S, C>)
        }

        fun <S, C : SelectionContext> build(
            filterSharedStorage: SharedStorage<Filter<C>>,
            block: GroupBuilder<S, C>.() -> Unit,
        ): Group<S, C> {
            val builder = GroupBuilderImpl<S, C>(filterSharedStorage).apply(block)
            val ret = GroupImpl(builder.pools, builder.filters, builder.default)
            return ret
        }
    }
}

interface GroupBuilder<S, C : SelectionContext> {
    val pools: MutableMap<String, Pool<S, C>>
    val filters: NodeContainer<Filter<C>> // TODO 应该设计一个 MutableNodeContainer. 原本的 NodeContainer 变成不可变的?
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
 * @param S the type of content
 * @param C the type of context
 */
abstract class GroupSerializer<S, C : SelectionContext> : SchemaSerializer<Group<S, C>> {
    companion object Constants {
        val HINT_NODE_SHARED_POOLS: RepresentationHint<ConfigurationNode> = RepresentationHint.of("node_shared_pools", typeTokenOf<ConfigurationNode>())
        private const val FILTERS_PATH = "filters"
        private const val SELECTS_PATH = "selects"
        private const val DEFAULT_PATH = "default"
    }

    protected abstract val globalFilters: SharedStorage<Filter<C>>

    protected abstract fun poolFactory(node: ConfigurationNode): Pool<S, C>
    protected abstract fun filterFactory(node: ConfigurationNode): Filter<C>

    final override fun deserialize(type: Type, node: ConfigurationNode): Group<S, C> {
        return Group.build(globalFilters) {
            when {
                // Node structure 1:
                // it's a list, which means it only has pools (no filters, no default)
                node.isMap && (!node.contains(FILTERS_PATH) && !node.contains(SELECTS_PATH) && !node.contains(DEFAULT_PATH)) -> {
                    decodePools(node, node)
                }

                // Node structure 2:
                // it's a map, which means it might have all components specified
                node.isMap -> {

                    node.node(FILTERS_PATH).takeUnless { it.virtual() }?.run node@{
                        filters.add builder@{
                            this@node.writeFiltersTo(this@builder)
                        }
                    }
                    node.node(SELECTS_PATH).takeUnless { it.virtual() }?.run node@{
                        decodePools(node, this@node)
                    }
                    node.node(DEFAULT_PATH).takeUnless { it.virtual() }?.run node@{
                        poolFactory(this@node)
                    }?.also {
                        this.default = it
                    }
                }

                // Unknown structure
                else -> {
                    // it's an unknown format

                    throw SerializationException(node.path(), type, "Unsupported format")
                }
            }
        }
    }

    private fun ConfigurationNode.extractKey(path: String): Key {
        val string = this.node(path).string ?: throw SerializationException(this, javaTypeOf<String>(), "The key is not specified")
        val key = runCatching { Key.key(string) }.getOrElse { throw SerializationException(this, javaTypeOf<Key>(), it) }
        return key
    }

    /**
     * 假设节点持有一个 [Node<Filter<C>>][Node] 的列表.
     */
    private fun ConfigurationNode.writeFiltersTo(builder: CompositeNode.NodeBuilder<Filter<C>>) {
        // 如果是这个 ConfigurationNode 是 virtual(),
        // 那么 ConfigurationNode#childrenList() 就会是一个空列表.
        // 也就是说, filters 这个 ConfigurationNode 可以在配置文件中完全省略.

        for (child in this.childrenList()) {
            val key = this.extractKey("type")
            if (key.namespace() == SharedStorage.NAMESPACE_GLOBAL) {
                // if `namespace` is "global", it's a `composite node`
                builder.composite(key)
                continue
            }

            // otherwise, it's a `local node`, we just create & put it
            builder.local(key, filterFactory(child))
        }
    }

    private fun GroupBuilder<S, C>.decodePools(groupNode: ConfigurationNode, selectNode: ConfigurationNode) {
        selectNode.childrenMap()
            .mapKeys { it.key.toString() }
            .forEach { (poolName, localPoolNode) ->
                val rawScalar = localPoolNode.rawScalar()
                if (rawScalar != null) {
                    // it's a raw string, meaning it's referencing a node in shared pools,
                    // so we need to pass the external node to the factory function
                    val sharedPoolsNode = groupNode.hint(HINT_NODE_SHARED_POOLS) ?: throw SerializationException(
                        selectNode, javaTypeOf<Group<S, C>>(), "Can't find hint ${HINT_NODE_SHARED_POOLS.identifier()}"
                    )
                    val referentPoolNode = sharedPoolsNode.node(rawScalar)
                    this.pools[poolName] = poolFactory(referentPoolNode)
                } else {
                    // it's not a raw string - we just pass the local node
                    this.pools[poolName] = poolFactory(localPoolNode)
                }
            }
    }
}


/* Implementations */


private object GroupEmpty : Group<Nothing, SelectionContext> {
    override val pools: Map<String, Pool<Nothing, SelectionContext>> = emptyMap()
    override val filters: NodeContainer<Filter<SelectionContext>> = NodeContainer.empty()
    override val default: Pool<Nothing, SelectionContext> = Pool.empty()
    override fun select(context: SelectionContext): List<Nothing> = Collections.emptyList()
}

private class GroupImpl<S, C : SelectionContext>(
    override val pools: Map<String, Pool<S, C>>,
    override val filters: NodeContainer<Filter<C>>,
    override val default: Pool<S, C>,
) : Group<S, C> {

    override fun select(context: C): List<S> {
        val isAllFiltersTrue = filters.all {
            it.test(context)
        }
        if (!isAllFiltersTrue) {
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
            return pool.pickBulk(context)
        }

        // pools 中没有一个满足条件的, 因此从 fallback 中选择
        return default.pickBulk(context)
    }
}

private class GroupBuilderImpl<S, C : SelectionContext>(
    globalFilters: SharedStorage<Filter<C>>,
) : GroupBuilder<S, C> {
    override val pools: MutableMap<String, Pool<S, C>> = LinkedHashMap() // pool list 需要遵循配置文件里的顺序, 因此必须为 LinkedHashMap
    override val filters: NodeContainer<Filter<C>> = NodeContainer(globalFilters)
    override var default: Pool<S, C> = Pool.empty()
}

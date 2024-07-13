package cc.mewcraft.wakame.random2

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
 * 同时, 该接口提供函数 [pickBulk], 让你从其中包含的所有 [pools][Pool]
 * 中随机选择一个 [S]. 关于具体的选择过程, 见函数 [pickBulk] 的说明.
 *
 * @param S 样本所携带的实例
 * @param C 条件所需要的上下文
 */
interface Group<S, C : SelectionContext> {

    // TODO: 我希望 Pool 是一个 Ref<Pool<S,C>>, Ref 是一个封装了 Pool 的类;
    //  这个 Ref 中的 Pool 可能来自于一个共享的地方, 也可能是一个独立的 Pool;
    //  Ref 中的 Pool 只能有一个, 因为这里的 Map 是一个 ID 对应一个 Pool.
    /**
     * 该 [group][Group] 包含的所有 [pools][Pool].
     */
    val pools: Map<String, Pool<S, C>>

    // TODO: 我希望 Filter 是一个 Ref<Filter<C>>, Ref 是一个封装了 Filter 的类;
    //  这个 Ref 中的 Filter 可能来自于一个共享的地方, 也可能是一个独立的 Filter;
    //  Ref 中的 Filter 可能只有一个, 也可能包含多个;
    //  因为这里最终是个 List, 所以本质上确实可以包含多个.
    /**
     * 进入该 [group][Group] 需要满足的条件. 多个条件为 AND 关系.
     */
    val filters: List<Filter<C>>

    // TODO: 我希望 Default 是一个 Ref<Pool<S,C>>, Ref 是一个封装了 Pool 的类;
    //  这个 Ref 只包含一个 Pool, 且这个 Pool 可能来自于一个共享的地方, 也可能是一个独立的 Pool.
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
    fun pickBulk(context: C): List<S>

    /**
     * The same as [pickBulk] but it only picks a **single** random [S]. In the
     * case where [pickBulk] returns an empty list, this function returns a `null`
     * instead.
     *
     * Use this function if you just want to pick a single [S].
     */
    fun pickSingle(context: C): S?

    companion object Factory {
        fun <S, C : SelectionContext> empty(): Group<S, C> {
            @Suppress("UNCHECKED_CAST")
            return (GroupEmpty as Group<S, C>)
        }

        fun <S, C : SelectionContext> build(block: GroupBuilder<S, C>.() -> Unit): Group<S, C> {
            val builder = GroupBuilderImpl<S, C>().apply(block)
            val ret = GroupImpl(
                pools = builder.pools,
                filters = builder.filters,
                default = builder.default
            )
            return ret
        }
    }
}

interface GroupBuilder<S, C : SelectionContext> {
    val pools: MutableMap<String, Pool<S, C>>
    val filters: MutableList<Filter<C>>
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

    protected abstract fun poolFactory(node: ConfigurationNode): Pool<S, C>
    protected abstract fun filterFactory(node: ConfigurationNode): Filter<C>

    final override fun deserialize(type: Type, node: ConfigurationNode): Group<S, C> {
        return Group.build {
            when {
                // Node structure 1
                node.isMap && (!node.contains(FILTERS_PATH) && !node.contains(SELECTS_PATH) && !node.contains(DEFAULT_PATH)) -> {
                    // it's a list, which means it only has pools (no filters, no default)

                    deserializeSelectsAndPut(node, node)
                }

                // Node structure 2
                node.isMap -> {
                    // it's a map, which means it might have all components specified

                    node.node(FILTERS_PATH).childrenList().forEach {
                        this.filters += filterFactory(it)
                    }
                    node.node(SELECTS_PATH).run {
                        deserializeSelectsAndPut(node, this)
                    }
                    node.node(DEFAULT_PATH).run {
                        if (this.virtual()) {
                            Pool.empty()
                        } else {
                            poolFactory(this)
                        }
                    }.also {
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

    private fun GroupBuilder<S, C>.deserializeSelectsAndPut(groupNode: ConfigurationNode, selectNode: ConfigurationNode) {
        selectNode.childrenMap().mapKeys { it.key.toString() }.forEach { (poolName, localPoolNode) ->
            val rawScalar = localPoolNode.rawScalar()
            if (rawScalar != null) {
                // it's a raw string, meaning it's referencing a node in shared pools,
                // so we need to pass the external node to the factory function
                val nodeSharedPools = groupNode.hint(HINT_NODE_SHARED_POOLS) ?: throw SerializationException(
                    selectNode, javaTypeOf<Group<S, C>>(), "Can't find hint ${HINT_NODE_SHARED_POOLS.identifier()}"
                )
                val nodeExternalPool = nodeSharedPools.node(rawScalar)
                this.pools[poolName] = poolFactory(nodeExternalPool)
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
    override val filters: List<Filter<SelectionContext>> = Collections.emptyList()
    override val default: Pool<Nothing, SelectionContext> = Pool.empty()

    override fun pickSingle(context: SelectionContext): Nothing? = null
    override fun pickBulk(context: SelectionContext): List<Nothing> = Collections.emptyList()
}

private class GroupImpl<S, C : SelectionContext>(
    override val pools: Map<String, Pool<S, C>>, // pool list 需要遵循配置文件里的顺序, 因此必须为 SequencedMap
    override val filters: List<Filter<C>>,
    override val default: Pool<S, C>,
) : Group<S, C> {

    override fun pickBulk(context: C): List<S> {
        val entryTest = filters.all { it.test(context) }
        if (!entryTest) {
            return emptyList() // 进入该 group 的条件未全部满足, 返回空
        }

        val pool = pools.values.firstOrNull { pool ->
            // 按顺序找到一个符合条件的 pool
            pool.filters.all { it.test(context) }
        }
        if (pool != null) {
            // 我们找到了一个满足条件的 pool, 因此从这个 pool 中选择
            return pool.pickBulk(context)
        }

        // pools 中没有一个满足条件的, 因此从 fallback 中选择
        return default.pickBulk(context)
    }

    override fun pickSingle(context: C): S? {
        return pickBulk(context).firstOrNull()
    }
}

private class GroupBuilderImpl<S, C : SelectionContext> : GroupBuilder<S, C> {
    override val pools: MutableMap<String, Pool<S, C>> = LinkedHashMap()
    override val filters: MutableList<Filter<C>> = ArrayList()
    override var default: Pool<S, C> = Pool.empty()
}

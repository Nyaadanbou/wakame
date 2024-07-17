package cc.mewcraft.wakame.random3

import me.lucko.helper.random.RandomSelector
import me.lucko.helper.random.Weigher
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type
import java.util.stream.Stream
import kotlin.random.asJavaRandom

/**
 * 一个基于权重的随机内容选择器.
 *
 * @param S 样本所携带的实例
 * @param C 条件所需要的上下文
 */
abstract class Pool<S, C : SelectionContext> {
    /**
     * 包含一些 [Pool] 的构造方法.
     */
    companion object Factory {
        fun <S, C : SelectionContext> empty(): Pool<S, C> {
            return PoolEmpty as Pool<S, C>
        }
    }

    /**
     * 每次随机抽取时, 抽几个 [Sample].
     */
    abstract val amount: Long

    /**
     * 该 [Pool] 包含的所有 [Sample].
     */
    abstract val samples: NodeContainer<Sample<S, C>>

    /**
     * 该 [Pool] 开始进行以及抽取前, 需要满足的所有条件.
     *
     * 如果有一个条件不满足, [select] 应该返回一个空列表.
     */
    abstract val filters: NodeContainer<Filter<C>>

    /**
     * 重置抽样 (Sampling with replacement)
     * - `true` = 重置抽样, 也就是抽取并放回
     * - `false` = 不重置抽样, 也就是抽取不放回
     */
    abstract val isReplacement: Boolean

    /**
     * 该 [Pool] 抽取一个样本时所调用的函数.
     */
    abstract fun whenSelect(value: S, context: C)

    /**
     * Randomly pick several [S] with given [context].
     *
     * Returns an empty list if:
     * - none of the [filters] are not met, or
     * - none of the samples meet their own filters
     */
    open fun select(context: C): List<S> {
        return PoolSupport.select(this, context)
    }

    /* Internal Implementations */

}

private object PoolSupport {
    // 结合
    fun <S, C : SelectionContext> select(pool: Pool<S, C>, context: C): List<S> {
        return select1(pool, context)
    }

    // 第一步
    private fun <S, C : SelectionContext> select0(pool: Pool<S, C>, context: C): Stream<Sample<S, C>> {
        // 检查进入该池的条件是否全部满足;
        // 如果没有全部满足, 直接返回空流.
        if (!pool.filters.all { it.test(context) }) {
            return Stream.empty()
        }

        // 筛选出满足条件的所有样本
        val samples = pool.samples.filter { sample ->
            sample.filters.all { filter ->
                filter.test(context)
            }
        }

        // 如果没有满足条件的样本, 返回空流
        if (samples.isEmpty()) {
            return Stream.empty()
        }

        // 设置是否重置抽样, 以及要选择的样本个数
        val stream = if (pool.isReplacement) {
            RandomSelector.weighted(samples, SampleWeigher)
                .stream(context.random.asJavaRandom())
                .limit(pool.amount)
        } else {
            // FIXME 虽然循环引用会导致添加重复的Node,
            //  但是在最终去重的时候好像又只会保留一个?
            //  这取决于 Sample 的 equals 的具体实现.
            RandomSelector.weighted(samples, SampleWeigher)
                .stream(context.random.asJavaRandom())
                .limit(pool.amount)
                .distinct()
        }

        return stream
    }

    // 第二步
    private fun <S, C : SelectionContext> select1(pool: Pool<S, C>, context: C): List<S> {
        return select0(pool, context)
            .toList()
            .onEach { it.marks?.run { context.marks += this } }
            .map { it.data }
    }
}

private object SampleWeigher : Weigher<Sample<*, *>> {
    override fun weigh(element: Sample<*, *>): Double {
        return element.weight
    }
}

/**
 * Shares the common code between pool serializers.
 *
 * The subclasses must assume that the config structure is either of the
 * following:
 *
 * ## Node structure 1 (simple)
 *
 * ```yaml
 * <node>:
 *   - type: <key>
 *     ...
 *     marks: x1y1z1
 *     weight: 1
 *     filters:
 *       - type: <key>
 *         ...
 *       - type: <key>
 *         ...
 *   - type: <key>
 *     ...
 *     marks: x2y2z2
 *     weight: 1
 *     filters:
 *       - type: <key>
 *         ...
 *       - type: <key>
 *         ...
 * ```
 *
 * ## Node structure 2 (completed)
 *
 * ```yaml
 * <node>:
 *   sample: <long>
 *   replacement: <boolean>
 *   filters:
 *     - type: <key>
 *       ...
 *     - type: <key>
 *     - ...
 *   entries:
 *     - type: <key>
 *       ...
 *       marks: x1y1z1
 *       weight: 1
 *       filters:
 *         - type: <key>
 *           ...
 *         - type: <key>
 *           ...
 *     - type: <key>
 *       ...
 *       marks: x2y2z2
 *       weight: 1
 *       filters:
 *         - type: <key>
 *           ...
 *         - type: <key>
 *           ...
 * ```
 *
 * @param V the type of sample
 * @param C the type of context
 */
abstract class PoolSerializer<V, C : SelectionContext> : TypeSerializer<Pool<V, C>> {

    protected abstract val sampleNodeFacade: SampleNodeFacade<V, C>
    protected abstract val filterNodeFacade: FilterNodeFacade<C>

    /**
     * [Pool] 的构造函数.
     */
    protected abstract fun poolConstructor(
        amount: Long,
        samples: NodeContainer<Sample<V, C>>,
        filters: NodeContainer<Filter<C>>,
        isReplacement: Boolean,
    ): Pool<V, C>

    /**
     * The factory to create value [V] from a [ConfigurationNode]. The
     * structure of the passed-in node is as following:
     * ```yaml
     * <node>:
     *   ... (完全取决于实现)
     * ```
     *
     * The `<impl_defined>` is what you need to take care of.
     *
     * @param node the configuration node
     * @return the content
     */
    protected open fun valueConstructor(node: ConfigurationNode): V? = null

    /**
     * The factory to create [Filter] from a [ConfigurationNode]. The
     * structure of the passed-in node is as following:
     * ```yaml
     * <node>:
     *   type: <filter_type>
     *   ...
     * ```
     *
     * @param node the configuration node
     * @return a new filter
     */
    protected open fun filterConstructor(node: ConfigurationNode): Filter<C> = Filter.alwaysTrue()

    /**
     * Defines the "intrinsic filters" of each sample in the pool.
     *
     * "Intrinsic filters" can be thought as those which will be
     * automatically added to the sample without specifically
     * configuring it in the configuration.
     *
     * @return the intrinsic filters
     */
    protected open fun intrinsicFilters(value: V): Filter<C> = Filter.alwaysTrue() // FIXME 应该返回一个 List<Filters<C>>

    /**
     * You should not override this.
     */
    final override fun deserialize(type: Type, node: ConfigurationNode): Pool<V, C> {
        when {
            // Node structure 1
            node.isList -> {
                val sampleNodeContainer = NodeContainer(sampleNodeFacade.repository) {
                    // 遍历当前 ListNode, 一个一个转成 random3.Node
                    for (listChild in node.childrenList()) {
                        // 设计上每一个 listChild 都是一个 random3.Node
                        node(sampleNodeFacade.decodeNode(listChild))
                    }
                }

                return poolConstructor(
                    amount = 1,
                    samples = sampleNodeContainer,
                    filters = NodeContainer.empty(),
                    isReplacement = false
                )
            }

            // Node structure 2
            node.isMap -> {
                val selectAmount = node.node("sample").getLong(1)
                val isReplacement = node.node("replacement").getBoolean(false)
                val sampleNodes = NodeContainer(sampleNodeFacade.repository) {
                    for (listChild in node.node("entries").childrenList()) {
                        node(sampleNodeFacade.decodeNode(listChild))
                    }
                }
                val filterNodes = NodeContainer(filterNodeFacade.repository) {
                    for (listChild in node.node("filters").childrenList()) {
                        node(filterNodeFacade.decodeNode(listChild))
                    }
                }

                return poolConstructor(
                    amount = selectAmount,
                    samples = sampleNodes,
                    filters = filterNodes,
                    isReplacement = isReplacement
                )
            }

            else -> {
                // it's an illegal structure
                throw SerializationException("Can't serialize pool ${node.path()} due to malformed structure")
            }
        }
    }

    /**
     * You should not override this.
     */
    final override fun serialize(type: Type, obj: Pool<V, C>?, node: ConfigurationNode) {
        throw UnsupportedOperationException()
    }
}

/* Implementations */

private object PoolEmpty : Pool<Nothing, SelectionContext>() {
    override val amount: Long = 1
    override val samples: NodeContainer<Sample<Nothing, SelectionContext>> = NodeContainer.empty()
    override val filters: NodeContainer<Filter<SelectionContext>> = NodeContainer.empty()
    override val isReplacement: Boolean = false
    override fun whenSelect(value: Nothing, context: SelectionContext) = Unit
    override fun select(context: SelectionContext): List<Nothing> = emptyList()
}

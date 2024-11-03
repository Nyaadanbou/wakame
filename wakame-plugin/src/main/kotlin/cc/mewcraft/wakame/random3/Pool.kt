@file:Suppress("UNCHECKED_CAST")

package cc.mewcraft.wakame.random3

import me.lucko.helper.random.RandomSelector
import me.lucko.helper.random.Weigher
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type
import kotlin.random.asJavaRandom

/**
 * 一个基于权重的随机内容选择器.
 *
 * @param S 样本所携带的实例
 * @param C 条件所需要的上下文
 */
abstract class Pool<S, C : RandomSelectorContext> {
    /**
     * 包含一些 [Pool] 的构造方法.
     */
    companion object Factory {
        fun <S, C : RandomSelectorContext> empty(): Pool<S, C> {
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
     * 基于上下文 [context] 随机选择一个或多个 [S].
     *
     * 将返回一个空列表, 如果:
     * - [filters] 中存在一个条件不满足, 或
     * - 所有 [samples] 都不满足自己的条件
     */
    open fun select(context: C): List<S> {
        return PoolSupport.select(this, context).onEach { whenSelect(it, context) }
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
abstract class PoolSerializer<V, C : RandomSelectorContext> : TypeSerializer<Pool<V, C>> {

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

    // /**
    //  * The function to create value [V] from a [ConfigurationNode]. The
    //  * structure of the passed-in node is as following:
    //  * ```yaml
    //  * <node>:
    //  *   ... (完全取决于实现)
    //  * ```
    //  *
    //  * The `<impl_defined>` is what you need to take care of.
    //  *
    //  * @param node the configuration node
    //  * @return the content
    //  */
    // protected open fun valueConstructor(node: ConfigurationNode): V? = null
    //
    // /**
    //  * The function to create [Filter] from a [ConfigurationNode]. The
    //  * structure of the passed-in node is as following:
    //  * ```yaml
    //  * <node>:
    //  *   type: <filter_type>
    //  *   ...
    //  * ```
    //  *
    //  * @param node the configuration node
    //  * @return a new filter
    //  */
    // protected open fun filterConstructor(node: ConfigurationNode): Filter<C> = Filter.alwaysTrue()
    //
    // /**
    //  * Defines the "intrinsic filters" of each sample in the pool.
    //  *
    //  * "Intrinsic filters" can be thought as those which will be
    //  * automatically added to the sample without specifically
    //  * configuring it in the configuration.
    //  *
    //  * @return the intrinsic filters
    //  */
    // protected open fun intrinsicFilters(value: V): Filter<C> = Filter.alwaysTrue()

    /**
     * You should not override this.
     */
    final override fun deserialize(type: Type, node: ConfigurationNode): Pool<V, C> {
        when {
            // Node structure 1
            // 这是一个纯粹的池映射, 没有过滤器, 也没有默认值
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
            // 这是一个完整的结构, 可能包含所有组件
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


private object PoolEmpty : Pool<Nothing, RandomSelectorContext>() {
    override val amount: Long = 1
    override val samples: NodeContainer<Sample<Nothing, RandomSelectorContext>> = NodeContainer.empty()
    override val filters: NodeContainer<Filter<RandomSelectorContext>> = NodeContainer.empty()
    override val isReplacement: Boolean = false
    override fun whenSelect(value: Nothing, context: RandomSelectorContext) = Unit
    override fun select(context: RandomSelectorContext): List<Nothing> = emptyList()
}

private object PoolSupport : KoinComponent {
    private val logger: Logger by inject()

    fun <S, C : RandomSelectorContext> select(pool: Pool<S, C>, context: C): List<S> {
        // 检查进入该池的条件是否全部满足;
        // 如果没有全部满足, 直接返回空流.
        if (!pool.filters.all { it.test(context) }) {
            return emptyList()
        }

        // 筛选出满足条件的所有样本
        val correctSamples = pool.samples.filter { sample ->
            sample.filters.all { filter ->
                filter.test(context)
            }
        }

        // 如果没有满足条件的样本, 返回空流
        if (correctSamples.isEmpty()) {
            return emptyList()
        }

        // 开发日记 24/11/3
        // 如果这里符合条件的样本数量小于要抽取的数量, 那么会导致程序无法跳出抽取样本的 while 循环.
        // 解决方案是在循环开始前, 确保 [要抽取的样本数量] 大于等于 [符合条件的样本数量].
        // 如果 [要抽取的样本数量] 小于 [符合条件的样本数量], 这说明这个池子的配置有问题.

        // 如果满足条件的样本数量小于要抽取的数量, 返回空流
        if (correctSamples.size < pool.amount) {
            logger.warn("The number of correct samples is less than the amount to be selected.")
            return emptyList()
        }

        // 最终要返回的 Sample
        val finalSamples: MutableList<Sample<S, C>> = arrayListOf()

        // 设置是否重置抽样, 以及要选择的样本个数
        if (pool.isReplacement) {
            // 抽取并放回

            val selector = RandomSelector.weighted(correctSamples, WEIGHER)
            while (finalSamples.size < pool.amount) {
                finalSamples += selector.pick(context.random.asJavaRandom())
            }

        } else {
            // 抽取不放回

            // 创建一个哈希集合, 用于存储已经抽取的样本
            val picked = hashSetOf<Sample<S, C>>()

            val selector = RandomSelector.weighted(correctSamples, WEIGHER)

            var count = 0
            while (picked.size < pool.amount) {
                count++
                if (count > 500) {
                    // 防止因为似循环而导致的严重问题, 记录日志并中断.
                    logger.warn("Stuck in an endless loop while selecting samples. This is a bug!")
                    break
                }

                val sample = selector.pick(context.random.asJavaRandom())
                if (sample !in picked) {
                    picked += sample
                }
            }

            finalSamples += picked
        }

        // 将所有 Sample 的 marks 添加到 context
        finalSamples.forEach {
            it.marks?.run { context.marks += this }
        }

        // 提取 Sample 中包含的数据
        val finalData = finalSamples.map { it.data }

        return finalData
    }

    private val WEIGHER = Weigher<Sample<*, *>>(Sample<*, *>::weight)
}
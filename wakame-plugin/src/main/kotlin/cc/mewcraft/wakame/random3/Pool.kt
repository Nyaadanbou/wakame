package cc.mewcraft.wakame.random3

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.util.javaTypeOf
import cc.mewcraft.wakame.util.krequire
import me.lucko.helper.random.RandomSelector
import me.lucko.helper.random.Weigher
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrElse
import kotlin.random.asJavaRandom

/**
 * 一个基于权重的随机内容选择器.
 *
 * @param S 样本所携带的实例
 * @param C 条件所需要的上下文
 */
interface Pool<S, C : SelectionContext> {

    /**
     * 每次随机时, 需要抽取几个 [sample][Sample].
     */
    val amount: Long

    /**
     * 该 [pool][Pool] 包含的所有 [sample][Sample].
     */
    val samples: NodeContainer<Sample<S, C>>

    /**
     * 进入该 [pool][Pool] 需要满足的条件. 多个条件为 `AND` 关系.
     *
     * 如果条件不满足:
     * - [pick] 将返回 `null`.
     * - [pickBulk] 将返回空列表.
     */
    val filters: NodeContainer<Filter<C>>

    /**
     * 重置抽样 (Sampling with replacement)
     * - `true` = 重置抽样, 也就是抽取并放回
     * - `false` = 不重置抽样, 也就是抽取不放回
     */
    val isReplacement: Boolean

    /**
     * Randomly pick several [S's][S] with given [context].
     *
     * Returns an empty list if:
     * - none of the [filters] are not met, or
     * - none of [samples][Sample] meet their own filters
     */
    fun pickBulk(context: C): List<S>

    /**
     * The same as [pickBulk] but it only picks a **single** random [S]. In the
     * case where [pickBulk] returns an empty list, this function returns `null`.
     *
     * Use this function if you know `this` pool should only pick a single [S].
     */
    fun pick(context: C): S?

    companion object Factory {
        fun <S, C : SelectionContext> empty(): Pool<S, C> {
            @Suppress("UNCHECKED_CAST")
            return (PoolEmpty as Pool<S, C>)
        }

        fun <S, C : SelectionContext> build(
            sampleSharedStorage: SharedStorage<Sample<S, C>>,
            filterSharedStorage: SharedStorage<Filter<C>>,
            block: PoolBuilder<S, C>.() -> Unit,
        ): Pool<S, C> {
            val builder = PoolBuilderImpl(sampleSharedStorage, filterSharedStorage).apply(block)
            val ret = PoolImpl(builder.amount, builder.samples, builder.filters, builder.isReplacement)
            return ret
        }
    }
}

/**
 * A [pool][Pool] builder.
 *
 * @param S the instance type wrapped in [sample][Sample]
 * @param C the context type required by [filters][Filter]
 */
interface PoolBuilder<S, C : SelectionContext> {
    var amount: Long
    val samples: NodeContainer<Sample<S, C>>
    val filters: NodeContainer<Filter<C>>
    var isReplacement: Boolean
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
 *    - <impl_defined>: <impl_defined>
 *      filters: # optional
 *        - type: <filter_type>
 *          <impl_defined>: <impl_defined>
 *          ...
 *        - type: <filter_type>
 *          <impl_defined>: <impl_defined>
 *          ...
 *      marks: x1y1z1 # optional
 *      weight: 1
 *    - <node with the same format as above>
 *    - ...
 * ```
 *
 * ## Node structure 2 (completed)
 *
 * ```yaml
 * <node>:
 *   sample: 1 # optional
 *   replacement: false # optional
 *   filters: # optional
 *     - type: <filter_type>
 *       <impl_defined>: <impl_defined>
 *       ...
 *     - <node with the same format as above>
 *     - ...
 *   entries:
 *     - <impl_defined>: <impl_defined>
 *       filters: # optional
 *         - type: <filter_type>
 *           <impl_defined>: <impl_defined>
 *           ...
 *         - <node with the same format as above>
 *         - ...
 *       marks: x1y1z1 # optional
 *       weight: 1
 *     - <node with the same format as above>
 *     - ...
 * ```
 *
 * @param S the type of sample
 * @param C the type of context
 */
abstract class PoolSerializer<S, C : SelectionContext> : SchemaSerializer<Pool<S, C>> {

    /**
     * 返回一个 [SharedStorage].
     */
    protected abstract val globalSamples: SharedStorage<Sample<S, C>>

    /**
     * 返回一个 [SharedStorage].
     */
    protected abstract val globalFilters: SharedStorage<Filter<C>>

    /**
     * The factory to create value [S] from a [ConfigurationNode]. The
     * structure of the passed-in node is as following:
     * ```yaml
     * <node>:
     *   <impl_defined>: <impl_defined>
     *   ...
     *   weight: 1
     *   mark: x1y1z1 # optional
     *   filters: # optional
     *     - type: <filter_type>
     *       <impl_defined>: <impl_defined>
     *     - <node with the same format as above>
     *     - ...
     * ```
     *
     * The `<impl_defined>` is what you need to take care of.
     *
     * @param node the configuration node
     * @return the content
     */
    protected open fun valueFactory(node: ConfigurationNode): S? = null

    /**
     * The factory to create [Filter] from a [ConfigurationNode]. The
     * structure of the passed-in node is as following:
     * ```yaml
     * type: <filter_type>
     * <impl_defined>: <impl_defined>
     *     ...
     * <impl_defined>: <impl_defined>
     * ```
     *
     * @param node the configuration node
     * @return a new filter
     */
    protected open fun filterFactory(node: ConfigurationNode): Filter<C> = Filter.alwaysTrue()

    /**
     * Defines the "intrinsic filters" of each sample in the pool.
     *
     * "Intrinsic filters" can be thought as those which will be
     * automatically added to the sample without specifically
     * configuring it in the configuration.
     *
     * @return the intrinsic filters
     */
    protected open fun intrinsicFilter(value: S): Filter<C> = Filter.alwaysTrue() // FIXME 应该返回一个 List<Filters<C>>

    /**
     * This function will be called upon the sample is picked.
     *
     * You can override this function if your selection needs
     * to leave some "traces" in the context.
     *
     * @param value the content wrapped in the [sample][Sample]
     * @param context the context
     */
    protected open fun whenPick(value: S, context: C) = Unit

    /**
     * This function will be called immediately before the pool is built.
     *
     * You can override this function if you need to specifically
     * tweak the settings of the pool that is being built.
     *
     * @param builder the pool builder
     */
    protected open fun whenBuild(builder: PoolBuilder<S, C>) = Unit

    /**
     * You should not override this.
     */
    final override fun deserialize(type: Type, node: ConfigurationNode): Pool<S, C> {
        when {
            // Node structure 1
            node.isList -> {
                return Pool.build(globalSamples, globalFilters) {
                    samples.add {
                        node.writeSamplesTo(this)
                    }

                    // apply builder overrides
                    whenBuild(this)
                }
            }

            // Node structure 2
            node.isMap -> {
                return Pool.build(globalSamples, globalFilters) {
                    samples.add {
                        node.node("entries").writeSamplesTo(this)
                    }
                    filters.add {
                        node.node("filters").writeFiltersTo(this)
                    }
                    amount = node.node("sample").getLong(1)
                    isReplacement = node.node("replacement").getBoolean(false)

                    // apply overrides
                    whenBuild(this)
                }
            }

            else -> {
                // it's an illegal structure
                throw SerializationException("Can't serialize pool ${node.path()} due to malformed structure")
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
    private fun ConfigurationNode.writeFiltersTo(builder: CompositeNode.Builder<Filter<C>>) {
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

    /**
     * 假设节点持有一个 [Node<Sample<S,C>>][Node] 的列表.
     */
    private fun ConfigurationNode.writeSamplesTo(builder: CompositeNode.Builder<Sample<S, C>>) {
        for (child in this.childrenList()) {
            val key = this.extractKey("key")
            if (key.namespace() == SharedStorage.NAMESPACE_GLOBAL) {
                // if `namespace` is "global", it's a `composite node`
                builder.composite(key)
                continue
            }

            // otherwise, it's a `local node`, we just create & put it
            val value = valueFactory(child)
            if (value == null) {
                continue
            }

            // wrap it into a sample
            val sample: Sample<S, C> = Sample(
                value = value,
                weight = child.node("weight").krequire<Double>(),
                filters = NodeContainer(globalFilters) {
                    // add intrinsic filters
                    local(key, intrinsicFilter(value))
                    // add custom filters
                    child.node("filters").writeFiltersTo(this)
                },
                marks = child.node("marks").string?.let { StringMark(it) },
                trace = { ctx ->
                    // 如果有 mark, 则添加 mark 到上下文
                    this.marks?.run { ctx.marks += this }
                    // 始终调用 whenPick
                    whenPick(this.value, ctx)
                }
            )

            builder.local(key, sample)
        }
    }
}


/* Implementations */


private object PoolEmpty : Pool<Nothing, SelectionContext> {
    override val amount: Long = 1
    override val samples: NodeContainer<Sample<Nothing, SelectionContext>> = NodeContainer.empty()
    override val filters: NodeContainer<Filter<SelectionContext>> = NodeContainer.empty()
    override val isReplacement: Boolean = true
    override fun pickBulk(context: SelectionContext): List<Nothing> = emptyList()
    override fun pick(context: SelectionContext): Nothing? = null
}

private class PoolImpl<S, C : SelectionContext>(
    override val amount: Long,
    override val samples: NodeContainer<Sample<S, C>>,
    override val filters: NodeContainer<Filter<C>>,
    override val isReplacement: Boolean,
) : Pool<S, C> {

    override fun pickBulk(context: C): List<S> {
        return pick0(context).map { sample ->
            // 将结果应用到 context
            sample.trace(sample, context)
            // 提取被样本封装的 S
            sample.value
        }.toList()
    }

    override fun pick(context: C): S? {
        val stream = pick0(context)
        val sample = stream.findAny().getOrElse {
            return null
        }
        // 将选择的结果应用到上下文
        sample.trace(sample, context)
        // 提取被样本封装的 S
        return sample.value
    }

    private fun pick0(context: C): Stream<Sample<S, C>> {
        val entryTest = filters.all {
            it.test(context)
        }
        // 检查进入该池的条件是否全部满足;
        // 如果没有全部满足, 直接返回空流.
        if (!entryTest) {
            return Stream.empty()
        }

        // 筛选出满足条件的所有样本
        val samples = samples.filter { sample ->
            sample.filters.all { filter ->
                filter.test(context)
            }
        }

        // 如果没有满足条件的样本, 返回空流
        if (samples.isEmpty()) {
            return Stream.empty()
        }

        val selector = RandomSelector.weighted(samples, SampleWeigher)

        // 设置是否重置抽样, 以及要选择的样本个数
        val stream = if (isReplacement) {
            selector.stream(context.random.asJavaRandom()).limit(amount)
        } else {
            // FIXME 虽然循环引用会导致添加重复的Node,
            //  但是在最终去重的时候好像又只会保留一个?
            //  这取决于 Sample 的 equals 的具体实现.
            selector.stream(context.random.asJavaRandom()).limit(amount).distinct()
        }

        return stream
    }
}

private class PoolBuilderImpl<S, C : SelectionContext>(
    samplesSharedStorage: SharedStorage<Sample<S, C>>,
    filtersSharedStorage: SharedStorage<Filter<C>>,
) : PoolBuilder<S, C> {
    override var amount: Long = 1
    override val samples: NodeContainer<Sample<S, C>> = NodeContainer(samplesSharedStorage)
    override val filters: NodeContainer<Filter<C>> = NodeContainer(filtersSharedStorage)
    override var isReplacement = false
}

private object SampleWeigher : Weigher<Sample<*, *>> {
    override fun weigh(element: Sample<*, *>): Double {
        return element.weight
    }
}

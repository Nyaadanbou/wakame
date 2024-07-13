package cc.mewcraft.wakame.random2

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.util.krequire
import me.lucko.helper.random.RandomSelector
import me.lucko.helper.random.Weigher
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

    // TODO: 首先, 这里直接声明为 Ref<Sample<S,C>>;
    //  Ref 是一个封装了 Sample 的类;
    //  其中可能包含0个或多个Sample.
    //  Ref 中的 Sample 可能来自于一个共享的地方, 也可能是几个独立的 Sample.
    /**
     * 该 [pool][Pool] 包含的所有 [sample][Sample].
     */
    val samples: List<Sample<S, C>>

    /**
     * 需要抽几个 [sample][Sample].
     */
    val pickAmount: Long

    // TODO: 我希望 Filter 是一个 Ref<Filter<C>>, Ref 是一个封装了 Filter 的类;
    //  这个 Ref 中的 Filter 可能来自于一个共享的地方, 也可能是一个独立的 Filter;
    //  Ref 中的 Filter 可能只有一个, 也可能包含多个;
    //  因为这里最终是个 List, 所以本质上确实可以包含多个.
    //  这里的机制应该跟 Group 中的一样.
    /**
     * 进入该 [pool][Pool] 需要满足的条件. 多个条件为 Logic AND 的关系.
     *
     * 如果条件不满足, 则 [pickBulk] 将直接返回 `null`.
     */
    val filters: List<Filter<C>>

    /**
     * 重置抽样 (Sampling with replacement)
     * - `true` = 重置抽样, 也就是抽取并放回
     * - `false` = 不重置抽样, 也就是抽取不放回
     */
    val isReplacement: Boolean

    /**
     * Randomly pick several [S's][S] with given [context]. Returns an empty
     * list if:
     * - none of the [filters] are not met, or
     * - none of [samples][Sample] meet their own filters
     */
    fun pickBulk(context: C): List<S>

    /**
     * The same as [pickBulk] but it only picks a **single** random [S]. In the
     * case where [pickBulk] returns an empty list, this function returns a `null`
     * instead.
     *
     * Use this function if you already know `this` pool can only pick a single
     * [S].
     */
    fun pickSingle(context: C): S?

    companion object Factory {
        fun <S, C : SelectionContext> empty(): Pool<S, C> {
            @Suppress("UNCHECKED_CAST")
            return (PoolEmpty as Pool<S, C>)
        }

        fun <S, C : SelectionContext> build(block: PoolBuilder<S, C>.() -> Unit): Pool<S, C> {
            val builder = PoolBuilderImpl<S, C>().apply(block)
            val ret = PoolImpl(
                samples = builder.samples,
                pickAmount = builder.pickAmount,
                isReplacement = builder.isReplacement,
                filters = builder.filters,
            )
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
    val samples: MutableList<Sample<S, C>>
    var pickAmount: Long
    var isReplacement: Boolean
    val filters: MutableList<Filter<C>>
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
 *      weight: 1
 *      filters: # optional
 *        - type: <filter_type>
 *          <impl_defined>: <impl_defined>
 *          ...
 *        - type: <filter_type>
 *          <impl_defined>: <impl_defined>
 *          ...
 *      mark: x1y1z1 # optional
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
 *       weight: 1
 *       filters: # optional
 *         - type: <filter_type>
 *           <impl_defined>: <impl_defined>
 *           ...
 *         - <node with the same format as above>
 *         - ...
 *       mark: x1y1z1 # optional
 *     - <node with the same format as above>
 *     - ...
 * ```
 *
 * @param S the type of sample
 * @param C the type of context
 */
abstract class PoolSerializer<S, C : SelectionContext> : SchemaSerializer<Pool<S, C>> {

    /**
     * The factory to create content [S] from a [ConfigurationNode]. The
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
    protected abstract fun sampleFactory(node: ConfigurationNode): S

    /**
     * **Subclasses may optionally override this.**
     *
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
     * **Subclasses may optionally override this.**
     *
     * Defines the "intrinsic filters" of each sample in the pool.
     *
     * "Intrinsic filters" can be thought as those which will be
     * automatically added to the sample without specifically configuring
     * it in the configuration.
     *
     * @return the intrinsic filters
     */
    protected open fun intrinsicFilters(content: S): Filter<C> = Filter.alwaysTrue()

    /**
     * **Subclasses may optionally override this.**
     *
     * This function will be called upon the sample is picked.
     *
     * You can override this function if your selection needs
     * to leave some "traces" in the context.
     *
     * @param content the content wrapped in the [sample][Sample]
     * @param context the context
     */
    protected open fun onPickSample(content: S, context: C) {}

    /**
     * **Subclasses may optionally override this.**
     *
     * This function will be called immediately before the pool is built.
     *
     * You can override this function if you need to specifically
     * tweak the settings of the pool that is being built.
     *
     * @param builder the pool builder
     */
    protected open fun onBuildPool(builder: PoolBuilder<S, C>) {}

    /**
     * Deserializes a list of filters from the node.
     */
    private fun deserializeFilters(node: ConfigurationNode): List<Filter<C>> {
        // if the node is virtual, the childrenList() will just be an empty list
        return node.childrenList().map(::filterFactory)
    }

    /**
     * Deserializes a list of samples from the node.
     */
    private fun deserializeSamples(node: ConfigurationNode): List<Sample<S, C>> {
        return node.childrenList().map { n ->
            // create sample content from the node
            val content = sampleFactory(n)

            // wrap it into a sample
            Sample.build(content) {
                weight = n.node("weight").krequire<Double>()

                // add intrinsic filters
                filters += intrinsicFilters(content)
                // add configured filters
                filters += deserializeFilters(n.node("filters"))

                // add mark if there is any
                mark = n.node("mark").string?.let { Mark.stringMarkOf(it) }

                // define trace function
                trace = {
                    // add the mark to context
                    mark?.run { it.marks += this }

                    // apply given trace function
                    onPickSample(content, it)
                }
            }
        }
    }

    final override fun deserialize(type: Type, node: ConfigurationNode): Pool<S, C> {
        when {
            // Node structure 1
            node.isList -> {
                return Pool.build {
                    samples += deserializeSamples(node)

                    // apply builder overrides
                    onBuildPool(this)
                }
            }

            // Node structure 2
            node.isMap -> {
                return Pool.build {
                    samples += deserializeSamples(node.node("entries"))
                    filters += deserializeFilters(node.node("filters"))
                    pickAmount = node.node("sample").getLong(1)
                    isReplacement = node.node("replacement").getBoolean(false)

                    // apply builder overrides
                    onBuildPool(this)
                }
            }

            else -> {
                // it's an illegal structure
                throw SerializationException("Can't serialize pool ${node.path()} due to illegal structure")
            }
        }
    }
}


/* Implementations */


private object PoolEmpty : Pool<Nothing, SelectionContext> {
    override val samples: List<Sample<Nothing, SelectionContext>> = emptyList()
    override val pickAmount: Long = 1
    override val isReplacement: Boolean = true
    override val filters: List<Filter<SelectionContext>> = emptyList()

    override fun pickBulk(context: SelectionContext): List<Nothing> = emptyList()
    override fun pickSingle(context: SelectionContext): Nothing? = null
}

private class PoolImpl<S, C : SelectionContext>(
    override val samples: List<Sample<S, C>>,
    override val pickAmount: Long,
    override val isReplacement: Boolean,
    override val filters: List<Filter<C>>,
) : Pool<S, C> {

    override fun pickBulk(context: C): List<S> {
        return pick0(context).map {
            it.trace(context) // 将结果应用到 context
            it.content // 提取被样本封装的 S
        }.toList()
    }

    override fun pickSingle(context: C): S? {
        val stream = pick0(context)
        val sample = stream.findAny().getOrElse {
            return null
        }

        sample.trace(context) // 将选择的结果应用到上下文
        return sample.content // 提取被样本封装的 S
    }

    private fun pick0(context: C): Stream<Sample<S, C>> {
        val entryTest = filters.all { it.test(context) }
        if (!entryTest) {
            return Stream.empty() // 进入该 pool 的条件未全部满足, 返回空
        }

        val samples = samples.filter { sample ->
            // 筛掉不满足条件的 sample
            sample.filters.all { it.test(context) }
        }
        if (samples.isEmpty()) {
            // 全都不满足条件, 返回空
            return Stream.empty()
        }

        val selector = RandomSelector.weighted(samples, SampleWeigher)
        // 设置是否重置抽样, 以及要选择的样本个数
        val stream = if (isReplacement) {
            selector.stream(context.random.asJavaRandom()).limit(pickAmount)
        } else {
            selector.stream(context.random.asJavaRandom()).limit(pickAmount).distinct()
        }

        return stream
    }
}

private class PoolBuilderImpl<S, C : SelectionContext> : PoolBuilder<S, C> {
    override val samples: MutableList<Sample<S, C>> = ArrayList()
    override var pickAmount: Long = 1
    override var isReplacement = false
    override val filters: MutableList<Filter<C>> = ArrayList()
}

private object SampleWeigher : Weigher<Sample<*, *>> {
    override fun weigh(element: Sample<*, *>): Double {
        return element.weight
    }
}

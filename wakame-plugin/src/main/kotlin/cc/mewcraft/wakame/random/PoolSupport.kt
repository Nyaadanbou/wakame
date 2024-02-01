package cc.mewcraft.wakame.random

import cc.mewcraft.wakame.condition.Condition
import me.lucko.helper.random.RandomSelector
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrElse

/**
 * An immutable [pool][Pool].
 */
internal class ImmutablePool<out T, C>(
    override val samples: List<Sample<T, C>>,
    override val pickCount: Long,
    override val conditions: List<Condition<C>>,
    override val isReplacement: Boolean,
) : Pool<T, C> {

    override fun pick(context: C): List<T> {
        val stream = pick0(context)
        if (stream.count() == 0L) {
            return emptyList()
        }

        return stream.map {
            it.applyContext(context) // 将结果应用到 context
            it.content // 提取被样本封装的 T
        }.toList()
    }

    override fun pickOne(context: C): T? {
        val stream = pick0(context)
        val sample = stream.findAny().getOrElse {
            return null
        }

        sample.applyContext(context) // 将选择的结果应用到上下文
        return sample.content // 提取被样本封装的 T
    }

    private fun pick0(context: C): Stream<Sample<T, C>> {
        val test = conditions.all { it.test(context) }
        if (!test) {
            return Stream.empty() // 进入该 pool 的条件未全部满足，返回空
        }

        val samples = samples.filter { sample ->
            // 筛掉不满足条件的 sample
            sample.conditions.all {
                it.test(context)
            }
        }
        if (samples.isEmpty()) {
            return Stream.empty()
        }

        val selector = RandomSelector.weighted(samples)

        // 设置是否重置抽样，以及要选择的样本个数
        val stream = if (isReplacement) {
            selector.stream().limit(pickCount)
        } else {
            selector.stream().limit(pickCount).distinct()
        }

        return stream
    }

    class Builder<out T, C> : Pool.Builder<T, C> {
        override var count: Long = 1
        override var replacement = false
        override val samples: MutableList<Sample<@UnsafeVariance T, C>> = ArrayList()
        override val conditions: MutableList<Condition<C>> = ArrayList()
    }
}

/**
 * A minimal empty [pool][Pool].
 */
internal object EmptyPool : Pool<Nothing, Any?> {
    override val pickCount: Long = 0
    override val isReplacement: Boolean = true
    override val samples: List<Sample<Nothing, Any?>> = emptyList()
    override val conditions: List<Condition<Any?>> = emptyList()
    override fun pick(context: Any?): List<Nothing> = emptyList()
    override fun pickOne(context: Any?): Nothing? = null
}
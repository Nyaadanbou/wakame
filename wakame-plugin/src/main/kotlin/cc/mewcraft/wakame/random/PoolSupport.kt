package cc.mewcraft.wakame.random

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.condition.Condition
import me.lucko.helper.random.RandomSelector
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrElse

/**
 * An immutable [pool][Pool].
 */
internal class ImmutablePool<S, C : SelectionContext>(
    override val samples: List<Sample<S, C>>,
    override val pickCount: Long,
    override val isReplacement: Boolean,
    override val conditions: List<Condition<C>>,
) : Pool<S, C> {

    override fun pick(context: C): List<S> {
        val stream = pick0(context)
        if (stream.count() == 0L) { // improve a bit performance
            return emptyList()
        }

        return stream.map {
            it.trace(context) // 将结果应用到 context
            it.content // 提取被样本封装的 S
        }.toList()
    }

    override fun pickOne(context: C): S? {
        val stream = pick0(context)
        val sample = stream.findAny().getOrElse {
            return null
        }

        sample.trace(context) // 将选择的结果应用到上下文
        return sample.content // 提取被样本封装的 S
    }

    private fun pick0(context: C): Stream<Sample<S, C>> {
        val entryConditionTest = conditions.all { it.test(context) }
        if (!entryConditionTest) {
            return Stream.empty() // 进入该 pool 的条件未全部满足，返回空
        }

        val samples = samples.filter { sample ->
            // 筛掉不满足条件的 sample
            sample.conditions.all {
                it.test(context)
            }
        }
        if (samples.isEmpty()) {
            // 全都不满足条件，返回空
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

    class Builder<S, C : SelectionContext> : Pool.Builder<S, C> {
        override val samples: MutableList<Sample<S, C>> = ArrayList()
        override var pickCount: Long = 1
        override var isReplacement = false
        override val conditions: MutableList<Condition<C>> = ArrayList()
    }
}

/**
 * A minimal empty [pool][Pool].
 */
@InternalApi
internal object EmptyPool : Pool<Nothing, SelectionContext> {
    override val samples: List<Sample<Nothing, SelectionContext>> = emptyList()
    override val pickCount: Long = 1
    override val isReplacement: Boolean = true
    override val conditions: List<Condition<SelectionContext>> = emptyList()

    override fun pick(context: SelectionContext): List<Nothing> = emptyList()
    override fun pickOne(context: SelectionContext): Nothing? = null
}
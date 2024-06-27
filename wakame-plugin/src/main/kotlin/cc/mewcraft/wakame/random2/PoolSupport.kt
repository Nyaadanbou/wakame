package cc.mewcraft.wakame.random2

import me.lucko.helper.random.RandomSelector
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrElse
import kotlin.random.asJavaRandom

internal class ImmutablePool<S, C : SelectionContext>(
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
            return Stream.empty() // 进入该 pool 的条件未全部满足，返回空
        }

        val samples = samples.filter { sample ->
            // 筛掉不满足条件的 sample
            sample.filters.all { it.test(context) }
        }
        if (samples.isEmpty()) {
            // 全都不满足条件，返回空
            return Stream.empty()
        }

        val selector = RandomSelector.weighted(samples, SampleWeigher)
        // 设置是否重置抽样，以及要选择的样本个数
        val stream = if (isReplacement) {
            selector.stream(context.random.asJavaRandom()).limit(pickAmount)
        } else {
            selector.stream(context.random.asJavaRandom()).limit(pickAmount).distinct()
        }

        return stream
    }
}

internal class PoolBuilderImpl<S, C : SelectionContext> : PoolBuilder<S, C> {
    override val samples: MutableList<Sample<S, C>> = ArrayList()
    override var pickAmount: Long = 1
    override var isReplacement = false
    override val filters: MutableList<Filter<C>> = ArrayList()
}

internal object EmptyPool : Pool<Nothing, SelectionContext> {
    override val samples: List<Sample<Nothing, SelectionContext>> = emptyList()
    override val pickAmount: Long = 1
    override val isReplacement: Boolean = true
    override val filters: List<Filter<SelectionContext>> = emptyList()

    override fun pickBulk(context: SelectionContext): List<Nothing> = emptyList()
    override fun pickSingle(context: SelectionContext): Nothing? = null
}
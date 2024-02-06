package cc.mewcraft.wakame.random

import cc.mewcraft.wakame.condition.Condition
import me.lucko.helper.random.Weighted

/**
 * 代表 [Pool] 中的一个样本。
 *
 * @param T 样本所携带的实例
 * @param C 条件所需要的上下文
 */
interface Sample<T, in C : SelectionContext> : Weighted {
    /**
     * The content wrapped within `this` [sample][Sample].
     */
    val content: T

    /**
     * The weight of `this` sample. Must be non-null.
     */
    val weight0: Double
    override fun getWeight(): Double = weight0 // save the Java implementation

    /**
     * 该 [sample][Sample] 被选中必须满足的条件。如果条件不满足，则该 [sample][Sample] 不会进入最终的样本空间当中。
     *
     * ## 条件将对样本概率产生直接影响
     *
     * 请注意，如果存在不满足条件的样本，那么最终的样本数量将少于原本设置的数量。又由于样本被选择的概率是基于总权重的，因此所有样本被选中的概率也会随之变化。
     */
    val conditions: List<Condition<C>>

    /**
     * Gets the [mark][Mark] of `this` [sample][Sample].
     */
    val mark: Mark<*>?

    /**
     * Leaves the "trace" of `this` sample to the [context].
     *
     * **This should be called upon `this` [sample][Sample] is picked.**
     */
    fun trace(context: C)

    /**
     * A sample builder.
     *
     * @param T the instance type wrapped in [sample][Sample]
     * @param C the context type required by [conditions][Condition]
     */
    interface Builder<T, C : SelectionContext> {
        val content: T
        var weight: Double
        var conditions: MutableList<Condition<C>>
        var mark: Mark<*>?
        var trace: (C) -> Unit
    }
}

inline fun <T, C : SelectionContext> buildSample(content: T, block: Sample.Builder<T, C>.() -> Unit): Sample<T, C> {
    val builder = SampleBuilder<T, C>(content).apply(block)
    val sample = object : AbstractSample<T, C>(
        content = builder.content,
        weight0 = builder.weight,
        conditions = builder.conditions,
        mark = builder.mark
    ) {
        override fun trace(context: C) {
            builder.trace(context)
        }
    }
    return sample
}

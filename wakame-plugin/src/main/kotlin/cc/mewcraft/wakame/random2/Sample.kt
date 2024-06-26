package cc.mewcraft.wakame.random2

/**
 * 代表 [Pool] 中的一个样本。
 *
 * @param S 样本所携带的实例
 * @param C 条件所需要的上下文
 */
interface Sample<S, in C : SelectionContext> {
    /**
     * The content wrapped within `this` [sample][Sample].
     */
    val content: S

    /**
     * The weight of `this` sample. Must be non-null.
     */
    val weight: Double

    /**
     * 该 [sample][Sample] 被选中必须满足的条件。如果条件不满足，则该 [sample][Sample] 不会进入最终的样本空间当中。
     *
     * ## 条件将对样本概率产生直接影响
     *
     * 请注意，如果存在不满足条件的样本，那么最终的样本数量将少于原本设置的数量。又由于样本被选择的概率是基于总权重的，因此所有样本被选中的概率也会随之变化。
     */
    val filters: List<Filter<C>>

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

    companion object Factory {
        inline fun <S, C : SelectionContext> build(content: S, block: SampleBuilder<S, C>.() -> Unit): Sample<S, C> {
            val builder = SampleBuilderImpl<S, C>(content).apply(block)
            val sample = object : AbstractSample<S, C>(
                content = builder.content,
                weight = builder.weight,
                filters = builder.filters,
                mark = builder.mark
            ) {
                override fun trace(context: C) {
                    builder.trace(context)
                }
            }
            return sample
        }
    }
}

/**
 * A sample builder.
 *
 * @param S the instance type wrapped in [sample][Sample]
 * @param C the context type required by [filters][Filter]
 */
interface SampleBuilder<S, C : SelectionContext>{
    val content: S
    var weight: Double
    var filters: MutableList<Filter<C>>
    var mark: Mark<*>?
    var trace: (C) -> Unit
}

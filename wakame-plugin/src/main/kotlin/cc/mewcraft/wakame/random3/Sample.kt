package cc.mewcraft.wakame.random3

/**
 * 代表 [Pool] 中的一个样本.
 *
 * @param S 样本所携带的实例
 * @param C 条件所需要的上下文
 */
class Sample<S, C : SelectionContext>(
    /**
     * The value wrapped within `this` [sample][Sample].
     */
    val value: S,

    /**
     * The weight of `this` sample. Must be non-null.
     */
    val weight: Double,

    /**
     * 该 [sample][Sample] 被选中必须满足的条件. 如果条件不满足, 则该 [sample][Sample] 不会进入最终的样本空间当中.
     *
     * ## 条件将对样本概率产生直接影响
     *
     * 请注意, 如果存在不满足条件的样本, 那么最终的样本数量将少于原本设置的数量.
     * 又由于样本被选择的概率是基于总权重的, 因此所有样本被选中的概率也会随之变化.
     */
    val filters: NodeContainer<Filter<C>>,

    /**
     * Gets the [mark][Mark].
     */
    val marks: StringMark?,

    /**
     * A function which leaves "trace" to the context [C].
     *
     * **This should be called upon this sample is picked.**
     */
    val trace: Sample<S, C>.(C) -> Unit = {},
)
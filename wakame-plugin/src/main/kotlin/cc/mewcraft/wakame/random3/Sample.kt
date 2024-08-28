package cc.mewcraft.wakame.random3

/**
 * 代表 [Pool] 中的一个样本.
 *
 * @param V 样本所携带的实例
 * @param C 条件所需要的上下文
 */
class Sample<V, C : SelectionContext>(
    /**
     * The data wrapped within `this` [Sample].
     */
    val data: V,

    /**
     * The weight of `this` sample. Must be non-null.
     */
    val weight: Double,

    /**
     * 该 [Sample] 被选中必须满足的条件. 如果条件不满足, 则该 [Sample] 不会进入最终的样本空间当中.
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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Sample<*, *>) return false
        if (data != other.data) return false

        // if (weight != other.weight) return false
        // if (filters != other.filters) return false
        // if (marks != other.marks) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }
}
package cc.mewcraft.wakame.random3

/**
 * 代表 [Pool] 中的一个样本.
 *
 * @param V 样本所携带的实例
 * @param C 条件所需要的上下文
 */
class Sample<V, C : RandomSelectorContext>(
    /**
     * 封装的数据.
     */
    val data: V,

    /**
     * 本样本的权重.
     */
    val weight: Double,

    /**
     * 该样本被选中必须满足的条件. 如果条件不满足, 则该样本不会进入最终的样本空间当中.
     *
     * ## 条件将对样本概率产生直接影响
     *
     * 请注意, 如果存在不满足条件的样本, 那么最终的样本数量将少于原本设置的数量.
     * 又由于样本被选择的概率是基于总权重的, 因此所有样本被选中的概率也会随之变化.
     */
    val filters: NodeContainer<Filter<C>>,

    /**
     * 返回该样本的 [Mark].
     */
    val marks: Mark?,
) {

    // 开发日记 2024/9/7
    // equals 和 hashCode 只比较 data

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other !is Sample<*, *>)
            return false
        if (data != other.data)
            return false
        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }
}
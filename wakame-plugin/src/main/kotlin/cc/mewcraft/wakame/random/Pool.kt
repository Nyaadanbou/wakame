package cc.mewcraft.wakame.random

import cc.mewcraft.wakame.condition.Condition

/**
 * 一个基于权重的随机内容选择器。
 *
 * @param T 样本所携带的实例
 * @param C 条件所需要的上下文
 */
interface Pool<out T, C> {

    /**
     * 该 [pool][Pool] 包含的所有 [Sample]。
     */
    val samples: List<Sample<T, C>>

    /**
     * 需要抽几个 [Sample]。
     */
    val pickCount: Long

    /**
     * 进入该 [pool][Pool] 需要满足的条件。多个条件为 Logic AND 的关系。
     *
     * 如果条件不满足，则 [pick] 将直接返回 `null`。
     */
    val conditions: List<Condition<C>>

    /**
     * 重置抽样 (Sampling with replacement)
     * - `true` = 重置抽样，也就是抽取并放回
     * - `false` = 不重置抽样，也就是抽取不放回
     */
    val isReplacement: Boolean

    /**
     * Randomly pick several [T]s with given [context]. Returns [EmptyList] if:
     * - none of the [conditions] are not met, or
     * - none of [samples][Sample] meet their own conditions
     */
    fun pick(context: C): List<T>

    /**
     * The same as [pick] but it only picks a **single** random [T]. In the
     * case where [pick] returns [EmptyList], this function returns a `null`
     * instead.
     *
     * Use this function if you already know `this` pool can only pick a single
     * [T].
     */
    fun pickOne(context: C): T?

    /**
     * A [pool][Pool] builder.
     *
     * @param T the instance type wrapped in [sample][Sample]
     * @param C the context type required by [conditions][Condition]
     */
    interface Builder<out T, C> {
        var count: Long
        var replacement: Boolean
        val samples: MutableList<Sample<@UnsafeVariance T, C>>
        val conditions: MutableList<Condition<C>>
    }
}

fun <T, C> emptyPool(): Pool<T, C> {
    @Suppress("UNCHECKED_CAST")
    return EmptyPool as Pool<T, C>
}

fun <T, C> buildPool(block: Pool.Builder<T, C>.() -> Unit): Pool<T, C> {
    val builder = ImmutablePool.Builder<T, C>().apply(block)
    val ret = ImmutablePool(
        samples = builder.samples,
        pickCount = builder.count,
        conditions = builder.conditions,
        isReplacement = builder.replacement,
    )
    return ret
}
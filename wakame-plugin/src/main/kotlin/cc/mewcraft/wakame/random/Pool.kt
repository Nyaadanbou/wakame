package cc.mewcraft.wakame.random

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.condition.Condition

/**
 * 一个基于权重的随机内容选择器。
 *
 * @param S 样本所携带的实例
 * @param C 条件所需要的上下文
 */
interface Pool<S, C : SelectionContext> {

    /**
     * 该 [pool][Pool] 包含的所有 [sample][Sample]。
     */
    val samples: List<Sample<S, C>>

    /**
     * 需要抽几个 [sample][Sample]。
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
     * Randomly pick several [S's][S] with given [context]. Returns an empty
     * list if:
     * - none of the [conditions] are not met, or
     * - none of [samples][Sample] meet their own conditions
     */
    fun pick(context: C): List<S>

    /**
     * The same as [pick] but it only picks a **single** random [S]. In the
     * case where [pick] returns an empty list, this function returns a `null`
     * instead.
     *
     * Use this function if you already know `this` pool can only pick a single
     * [S].
     */
    fun pickOne(context: C): S?

    companion object Factory {
        fun <S, C : SelectionContext> empty(): Pool<S, C> = @OptIn(InternalApi::class) @Suppress("UNCHECKED_CAST") (EmptyPool as Pool<S, C>)

        fun <S, C : SelectionContext> build(block: PoolBuilder<S, C>.() -> Unit): Pool<S, C> {
            val builder = PoolBuilderImpl<S, C>().apply(block)
            val ret = ImmutablePool(
                samples = builder.samples,
                pickCount = builder.pickCount,
                isReplacement = builder.isReplacement,
                conditions = builder.conditions,
            )
            return ret
        }
    }
}

/**
 * A [pool][Pool] builder.
 *
 * @param S the instance type wrapped in [sample][Sample]
 * @param C the context type required by [conditions][Condition]
 */
interface PoolBuilder<S, C : SelectionContext> {
    val samples: MutableList<Sample<S, C>>
    var pickCount: Long
    var isReplacement: Boolean
    val conditions: MutableList<Condition<C>>
}
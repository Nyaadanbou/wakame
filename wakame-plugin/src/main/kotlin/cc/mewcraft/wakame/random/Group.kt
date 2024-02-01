package cc.mewcraft.wakame.random

import cc.mewcraft.wakame.condition.Condition
import java.util.SequencedMap

/**
 * [Group] 是一个包含了若干 [Pool] 的集合。同时，该接口提供函数 [pick]，让你从其中包含的所有 [pools][Pool]
 * 中随机选择一个 [T]。关于具体的选择过程，见函数 [pick] 的说明。
 *
 * @param T 样本所携带的实例
 * @param C 条件所需要的上下文
 */
interface Group<out T, C> {

    /**
     * 进入该 [group][Group] 需要满足的条件。多个条件为 AND 关系。
     */
    val conditions: List<Condition<C>>

    /**
     * 该 [group][Group] 包含的所有 [pools][Pool]。
     */
    val pools: SequencedMap<String, Pool<@UnsafeVariance T, C>>

    /**
     * The fallback [pool][Pool] to be chosen when nothing is picked from
     * [pools].
     */
    val fallbackPool: Pool<T, C>

    /**
     * Randomly pick several [T]s with given [context]. Returns [EmptyList] if
     * [conditions] are not met **AND** the [fallbackPool] pool also returns a
     * `null`.
     *
     * ## 随机抽取的步骤
     * 1. 首先检查 `this` 的 [conditions]
     *    * 如果条件满足，则继续接下来的操作
     *    * 如果条件不满足，则直接返回 [EmptyList]
     * 2. 按顺序从 `this` 的 [pools] 选择第一个满足条件的池，然后调用 [Pool.pick] 来选择最终的样本
     *    * 如果 [pools][Pool] 中没有满足条件的，将从 [fallbackPool] 中选择一个结果
     *    * 如果 [fallbackPool] 也没有结果，最终将返回 [EmptyList]
     */
    fun pick(context: C): List<T>

    /**
     * The same as [pick] but it only picks a **single** random [T]. In the
     * case where [pick] returns [EmptyList], this function returns a `null`
     * instead.
     *
     * Use this function if you already know `this` group can only pick a
     * single [T].
     */
    fun pickOne(context: C): T?

    interface Builder<out T, C> {
        val pools: SequencedMap<String, Pool<@UnsafeVariance T, C>>
        val conditions: MutableList<Condition<C>>
        var fallback: Pool<@UnsafeVariance T, C>
    }
}

fun <T, C> emptyGroup(): Group<T, C> {
    @Suppress("UNCHECKED_CAST")
    return EmptyGroup as Group<T, C>
}

fun <T, C> buildGroup(block: Group.Builder<T, C>.() -> Unit): Group<T, C> {
    val builder = ImmutableGroup.Builder<T, C>().apply(block)
    val ret = ImmutableGroup(
        pools = builder.pools,
        conditions = builder.conditions,
        fallbackPool = builder.fallback
    )
    return ret
}

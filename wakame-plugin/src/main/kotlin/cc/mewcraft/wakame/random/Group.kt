package cc.mewcraft.wakame.random

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.condition.Condition
import java.util.SequencedMap

/**
 * [Group] 是一个包含了若干 [Pool] 的集合。同时，该接口提供函数 [pick]，让你从其中包含的所有 [pools][Pool]
 * 中随机选择一个 [S]。关于具体的选择过程，见函数 [pick] 的说明。
 *
 * @param S 样本所携带的实例
 * @param C 条件所需要的上下文
 */
interface Group<S, C : SelectionContext> {

    /**
     * 该 [group][Group] 包含的所有 [pools][Pool]。
     */
    val pools: SequencedMap<String, Pool<S, C>>

    /**
     * 进入该 [group][Group] 需要满足的条件。多个条件为 AND 关系。
     */
    val conditions: List<Condition<C>>

    /**
     * The fallback [pool][Pool] when nothing is picked from [pools].
     */
    val default: Pool<S, C>

    /**
     * Randomly pick several [S's][S] with given [context]. Returns an empty
     * list if:
     * - [conditions][conditions] are not met, and
     * - [fallback pool][default] returns a `null`.
     *
     * ## 随机抽取的步骤
     * 1. 首先检查 `this` 的 [conditions][conditions]
     *    * 如果条件满足，则继续接下来的操作
     *    * 如果条件不满足，则直接返回空列表
     * 2. 按顺序从 `this` 的 [pools] 选择第一个满足条件的池，然后调用 [Pool.pick] 来选择最终的样本
     *    * 如果 [pools][Pool] 中没有满足条件的，将从 [fallback pool][default] 中选择一个结果
     *    * 如果 [fallback pool][default] 也没有结果，最终将返回空列表
     */
    fun pick(context: C): List<S>

    /**
     * The same as [pick] but it only picks a **single** random [S]. In the
     * case where [pick] returns an empty list, this function returns a `null`
     * instead.
     *
     * Use this function if you just want to pick a single [S].
     */
    fun pickSingle(context: C): S?

    companion object Factory {
        fun <S, C : SelectionContext> empty(): Group<S, C> = @OptIn(InternalApi::class) @Suppress("UNCHECKED_CAST") (EmptyGroup as Group<S, C>)

        fun <S, C : SelectionContext> build(block: GroupBuilder<S, C>.() -> Unit): Group<S, C> {
            val builder = GroupBuilderImpl<S, C>().apply(block)
            val ret = ImmutableGroup(
                pools = builder.pools,
                conditions = builder.conditions,
                default = builder.default
            )
            return ret
        }
    }
}

interface GroupBuilder<S, C : SelectionContext> {
    val pools: SequencedMap<String, Pool<S, C>>
    val conditions: MutableList<Condition<C>>
    var default: Pool<S, C>
}
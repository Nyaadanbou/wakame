package cc.mewcraft.wakame.random

import cc.mewcraft.wakame.condition.Condition
import java.util.Collections
import java.util.SequencedMap

/**
 * An immutable [group][Group].
 */
internal class ImmutableGroup<T, C>(
    // pool list 需要遵循配置文件里的顺序，因此必须为 SequencedMap
    override val pools: SequencedMap<String, Pool<T, C>>,
    override val conditions: List<Condition<C>>,
    override val fallbackPool: Pool<T, C>,
) : Group<T, C> {

    override fun pick(context: C): List<T> {
        val test = conditions.all { it.test(context) }
        if (!test) {
            return emptyList() // 进入该 group 的条件未全部满足，返回空
        }

        val pool = pools.values.firstOrNull { pool ->
            // 按顺序，找到一个符合条件的 pool
            pool.conditions.all {
                it.test(context)
            }
        }
        if (pool != null) {
            // 我们找到了一个满足条件的 pool，因此从这个 pool 中选择
            return pool.pick(context)
        }

        // pools 中没有一个满足条件的，因此从 fallback 中选择
        return fallbackPool.pick(context)
    }

    override fun pickOne(context: C): T? {
        return pick(context).firstOrNull()
    }

    class Builder<T, C> : Group.Builder<T, C> {
        override val pools: SequencedMap<String, Pool<T, C>> = LinkedHashMap()
        override val conditions: MutableList<Condition<C>> = ArrayList()
        override var fallback: Pool<T, C> = emptyPool()
    }

}

/**
 * A minimal empty [group][Group].
 */
internal object EmptyGroup : Group<Nothing, Any?> {
    override val pools: SequencedMap<String, Pool<Nothing, Any?>> = Collections.emptySortedMap()
    override val conditions: List<Condition<Any?>> = emptyList()
    override val fallbackPool: Pool<Nothing, Any?> = emptyPool()
    override fun pick(context: Any?): List<Nothing> = emptyList()
    override fun pickOne(context: Any?): Nothing? = null
}
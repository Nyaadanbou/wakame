package cc.mewcraft.wakame.random

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.condition.Condition
import java.util.Collections
import java.util.SequencedMap

/**
 * An immutable [group][Group].
 */
internal class ImmutableGroup<S, C : SelectionContext>(
    // pool list 需要遵循配置文件里的顺序，因此必须为 SequencedMap
    override val pools: SequencedMap<String, Pool<S, C>>,
    override val conditions: List<Condition<C>>,
    override val default: Pool<S, C>,
) : Group<S, C> {

    override fun pick(context: C): List<S> {
        val entryConditionTest = conditions.all { it.test(context) }
        if (!entryConditionTest) {
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
        return default.pick(context)
    }

    override fun pickOne(context: C): S? {
        return pick(context).firstOrNull()
    }

    class Builder<S, C : SelectionContext> : Group.Builder<S, C> {
        override val pools: SequencedMap<String, Pool<S, C>> = LinkedHashMap()
        override val conditions: MutableList<Condition<C>> = ArrayList()
        override var default: Pool<S, C> = Pool.empty()
    }
}

/**
 * A minimal empty [group][Group].
 */
@InternalApi
internal object EmptyGroup : Group<Nothing, SelectionContext> {
    override val pools: SequencedMap<String, Pool<Nothing, SelectionContext>> = Collections.emptySortedMap()
    override val conditions: List<Condition<SelectionContext>> = Collections.emptyList()
    override val default: Pool<Nothing, SelectionContext> = Pool.empty()

    override fun pickOne(context: SelectionContext): Nothing? = null
    override fun pick(context: SelectionContext): List<Nothing> = Collections.emptyList()
}
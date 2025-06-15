package cc.mewcraft.wakame.random4

import cc.mewcraft.wakame.random4.context.LootContext
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.require

fun <S> LootTable(
    pools: List<LootPool<S>>
): LootTable<S> = SimpleLootTable(pools)

/**
 * [LootTable] 是一个包含了若干 [LootPool] 的集合
 */
interface LootTable<S> {
    companion object {
        val SERIALIZER: TypeSerializer2<LootTable<*>> = TypeSerializer2 { type, node -> node.require<SimpleLootTable<*>>() }
    }

    /**
     * 这个 [LootTable] 中的所有 [LootPool].
     */
    val pools: List<LootPool<S>>

    /**
     * 选择 [LootTable] 中的样本.
     */
    fun select(context: LootContext): List<S>
}

/* Implementations */

private data class SimpleLootTable<S>(
    override val pools: List<LootPool<S>>,
) : LootTable<S> {
    override fun select(context: LootContext): List<S> {
        val result = mutableListOf<S>()
        val correctPools = pools.filter { pool ->
            // 过滤掉不满足条件的 pool
            pool.conditions.all { it.test(context) }
        }

        for (pool in correctPools) {
            // 我们找到了一个满足条件的 pool, 因此将这个 pool 选择的结果添加到结果列表中
            result += pool.select(context)
        }

        return result
    }
}
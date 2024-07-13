package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.util.ThreadLocalCyclingCounter

/**
 * 用于构建需要“重复渲染”的 [LoreLine].
 *
 * ## 开发日记 2024/7/13
 * 由于 [LoreLineFlatter] 内部的算法限制, 如果多个 [LoreLine] 如果拥有相同的索引,
 * 那么最终只有第一个 [LoreLine] 会被渲染出来. 为了解决这个问题, 我们设计了 [CyclingLoreLineProvider],
 * 只需要调用 [next] 就可以获得一个内容一致但索引不同的 [LoreLine]. 同时为了更高的性能,
 * [next] 返回的 [LoreLine] 实际上是从一个对象池中获取的, 并且会根据调用的顺序进行循环.
 */
internal class CyclingLoreLineProvider(
    /**
     * The limit of the cycling lore line pool.
     */
    limit: Int,
    /**
     * The provider function that provides a [LoreLine] based on the index.
     */
    private val provider: (Int) -> LoreLine,
) {
    private val cyclingLoreLinePool: Array<LoreLine?> = arrayOfNulls(limit)
    private val cyclingIntegerCounter: ThreadLocalCyclingCounter = ThreadLocalCyclingCounter(limit)

    /**
     * Provides a [LoreLine] based on the cycling index.
     */
    fun next(): LoreLine {
        val idx = cyclingIntegerCounter.next()
        val line = cyclingLoreLinePool[idx]
        if (line == null) {
            val newLine = provider(idx)
            cyclingLoreLinePool[idx] = newLine
            return newLine
        }
        return line
    }
}

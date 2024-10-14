package cc.mewcraft.wakame.display2

import cc.mewcraft.wakame.util.ThreadLocalCounterCycle

/**
 * 用于构建需要“重复渲染”的 [IndexedText].
 *
 * ## 开发日记 2024/7/13
 * 由于 [IndexedTextListTransformer] 内部的算法限制, 如果多个 [IndexedText] 如果拥有相同的索引,
 * 那么最终只有第一个 [IndexedText] 会被渲染出来. 为了解决这个问题, 我们设计了 [IndexedTextCycle],
 * 只需要调用 [next] 就可以获得一个内容一致但索引不同的 [IndexedText]. 同时为了更高的性能,
 * [next] 返回的 [IndexedText] 实际上是从一个对象池中获取的, 并且会根据调用的顺序进行循环.
 */
internal class IndexedTextCycle(
    /**
     * The limit of the cycling lore line pool.
     */
    private val limit: Int,
    /**
     * The provider function that provides a [IndexedText] based on the index.
     */
    private val provider: (Int) -> IndexedText,
) {
    private val cyclingIndexedTextPool: Array<IndexedText?> = arrayOfNulls(limit)
    private val cyclingIntegerCounter: ThreadLocalCounterCycle = ThreadLocalCounterCycle(limit)

    /**
     * Provides a [IndexedText] based on the cycling index.
     */
    fun next(): IndexedText {
        val idx = cyclingIntegerCounter.next()
        val line = cyclingIndexedTextPool[idx]
        if (line == null) {
            val newLine = provider(idx)
            cyclingIndexedTextPool[idx] = newLine
            return newLine
        }
        return line
    }

    override fun toString(): String {
        return "IndexedTextCycle(limit=$limit)"
    }
}

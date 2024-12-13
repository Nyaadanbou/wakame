// 文件说明:
// 由于索引相同的 IndexedText 经过 TextAssembler 的处理后会去重,
// 这里提供了一套通用的实现, 用来循环产生在末尾带有序数的 IndexedText#idx
// 使得索引不再重复, 最终实现在渲染结果中出现多个相同内容的 IndexedText.


package cc.mewcraft.wakame.display2.implementation.common

import cc.mewcraft.wakame.display2.DerivedIndex
import cc.mewcraft.wakame.display2.IndexedText
import cc.mewcraft.wakame.display2.SimpleIndexedText
import cc.mewcraft.wakame.display2.SimpleTextMeta
import cc.mewcraft.wakame.display2.SourceIndex
import cc.mewcraft.wakame.display2.SourceOrdinal
import cc.mewcraft.wakame.display2.TextAssembler
import cc.mewcraft.wakame.display2.TextMetaFactory
import cc.mewcraft.wakame.display2.implementation.common.CyclicTextMeta.Shared.MAX_DISPLAY_COUNT
import cc.mewcraft.wakame.util.ThreadLocalCounterCycle
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

internal data class CyclicTextMeta(
    override val sourceIndex: SourceIndex,
    override val sourceOrdinal: SourceOrdinal,
    override val defaultText: List<Component>?,
    private val indexRule: CyclicIndexRule,
) : SimpleTextMeta {
    override val derivedIndexes: List<DerivedIndex> = deriveIndexes()

    /**
     * 根据 [MAX_DISPLAY_COUNT] 生成对应数量的 [DerivedIndex].
     *
     * 如果生成算法采用的是 [CyclicIndexRule.SLASH],
     * 那么生成的结果将类似下面这样:
     * - `namespace:value/0`
     * - `namespace:value/1`
     * - `namespace:value/2`
     * - `...`
     */
    override fun deriveIndexes(): List<DerivedIndex> {
        val ret = mutableListOf<DerivedIndex>()
        for (i in 0 until MAX_DISPLAY_COUNT) {
            ret += indexRule.make(sourceIndex, i)
        }
        return ret
    }

    companion object Shared {
        const val MAX_DISPLAY_COUNT = 16
    }
}

internal data class CyclicTextMetaFactory(
    private val namespace: String,
    private val id: String,
    private val indexRule: CyclicIndexRule,
) : TextMetaFactory {
    override fun create(sourceIndex: SourceIndex, sourceOrdinal: SourceOrdinal, defaultText: List<Component>?): SimpleTextMeta {
        return CyclicTextMeta(sourceIndex, sourceOrdinal, defaultText, indexRule)
    }
}

/**
 * 用于构建需要“重复渲染”的 [IndexedText].
 *
 * ## 开发日记 2024/7/13
 * 由于 [TextAssembler] 内部的算法限制, 如果多个 [IndexedText] 如果拥有相同的索引,
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
    private val provider: (Int) -> SimpleIndexedText,
) {
    private val cyclingIndexedTextPool: Array<SimpleIndexedText?> = arrayOfNulls(limit)
    private val cyclingIntegerCounter: ThreadLocalCounterCycle = ThreadLocalCounterCycle(limit)

    /**
     * Provides a [IndexedText] based on the cycling index.
     */
    fun next(): SimpleIndexedText {
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

/**
 * 代表一个循环索引的生成规则.
 */
internal fun interface CyclicIndexRule {
    /**
     * 以给定的索引 [i] 和循环序数 [n] 生成一个循环索引.
     */
    fun make(i: Key, n: Int): Key

    companion object Shared {
        /**
         * 用 `/` 分割原本的索引, 然后加上循环出现的序号.
         */
        @JvmField
        val SLASH: CyclicIndexRule = CyclicIndexRule { i, n ->
            Key.key("${i.namespace()}:${i.value()}/$n")
        }
    }
}
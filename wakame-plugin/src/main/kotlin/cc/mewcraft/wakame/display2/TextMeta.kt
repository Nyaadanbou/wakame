package cc.mewcraft.wakame.display2

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

/**
 * 代表 [IndexedText] 的元数据.
 *
 * 元数据指的是用户给特定内容.
 */
interface TextMeta {
    /**
     * 原始索引 (即配置文件的列表中未经衍生的字符串).
     */
    val sourceIndex: SourceIndex

    /**
     * 原始序数 (即配置文件的列表中字符串所在的顺序).
     */
    val sourceOrdinal: SourceOrdinal

    /**
     * 生成全部的 [DerivedIndex].
     *
     * ## 用途
     * [DerivedIndex] 将被直接用于查询 [IndexedText] 的顺序.
     *
     * ## 来源
     * [DerivedIndex] 是由 [SourceIndex] 经过一系列规则衍生出来的一个或多个值.
     *
     * ## 性质
     * 某些 [SourceIndex] 没有衍生规则, 因此这些 [SourceIndex] 在结果上与 [DerivedIndex]
     * 完全一致. 这种情况下 [DerivedIndex] 只是个单例列表. 而有些 [SourceIndex]
     * 存在衍生规则, 因此它们的 [SourceIndex] 就与 [DerivedIndex] 不一致.
     */
    fun generateIndexes(): List<DerivedIndex>

    /**
     * 生成从 [DerivedIndex] 到 [DerivedOrdinal] 的映射.
     *
     * 该映射将被直接用于查询 [IndexedText] 在物品提示框中的顺序.
     *
     * 该函数的参数 [offset] 为索引的偏移量. 你必须传入正确的 [offset], 否则该函数生成的 [DerivedOrdinal] 将是错的.
     *
     * 为了更好的理解 [offset] 的取值, 这里举个例子. 假设配置文件中有以下三行内容, 我们标记好各自的 [SourceOrdinal]:
     * - a ([SourceOrdinal] = 1)
     * - b ([SourceOrdinal] = 2)
     * - c ([SourceOrdinal] = 3)
     *
     * 经过衍生后的内容如下：
     * - a.1 ([SourceOrdinal] = 1)
     * - a.2 ([SourceOrdinal] = 1)
     * - b.1 ([SourceOrdinal] = 2)
     * - b.2 ([SourceOrdinal] = 2)
     * - c   ([SourceOrdinal] = 3)
     *
     * [DerivedOrdinal] 的计算方式为 [SourceOrdinal] + `local index` + `offset`. 其中 `local index` 是
     * [DerivedOrdinal] 的索引值. 假设 [offset] 为 0；现在基于 [SourceOrdinal], 为每个内容生成 [DerivedOrdinal]:
     * - a.1 ([SourceOrdinal] = 1, `offset` = 0 -> [DerivedOrdinal] = 1)
     * - a.2 ([SourceOrdinal] = 1, `offset` = 0 -> [DerivedOrdinal] = 2)
     * - b.1 ([SourceOrdinal] = 2, `offset` = 0 -> [DerivedOrdinal] = 2)
     * - b.2 ([SourceOrdinal] = 2, `offset` = 0 -> [DerivedOrdinal] = 3)
     * - c   ([SourceOrdinal] = 3, `offset` = 0 -> [DerivedOrdinal] = 3)
     *
     * 可以看到这里重复的 [DerivedOrdinal], 而这显然是错的. 导致该问题的原因是每一个内容的生成都假设它之前**不存在**衍生的内容.
     *
     * 为了解决这个问题, 我们引入 [offset] 的概念, 用来对生成的 [DerivedOrdinal] 进行偏移, 以实现整体上的正确性.
     *
     * 假设给定的 [offset] 都是正确的, 那么重新生成以上内容应该得到:
     * - a.1 ([SourceOrdinal] = 1, `offset` = 0 -> [DerivedOrdinal] = 1)
     * - a.2 ([SourceOrdinal] = 1, `offset` = 0 -> [DerivedOrdinal] = 2)
     * - b.1 ([SourceOrdinal] = 2, `offset` = 1 -> [DerivedOrdinal] = 3)
     * - b.2 ([SourceOrdinal] = 2, `offset` = 1 -> [DerivedOrdinal] = 4)
     * - c   ([SourceOrdinal] = 3, `offset` = 2 -> [DerivedOrdinal] = 5)
     *
     * @param offset [DerivedOrdinal] 的偏移量
     */
    fun generateOrdinals(offset: Int): Map<DerivedIndex, DerivedOrdinal> {
        val index2Ordinal = LinkedHashMap<DerivedIndex, DerivedOrdinal>() // for debug inspection
        for ((localOrdinal, fullKey) in generateIndexes().withIndex()) {
            index2Ordinal[fullKey] = sourceOrdinal + localOrdinal + offset
        }
        return index2Ordinal
    }
}

/**
 * 用来描述数据来源于物品堆叠本身的 [IndexedText].
 *
 * 这基本包括了物品的 `lore`, `level` 等一切
 */
interface SimpleTextMeta : TextMeta {
    /**
     * 内容的默认值.
     *
     * ## 用途
     * 如果源数据不存在, 将显示默认值, 而不是直接跳过显示.
     *
     * ## 空值约定
     * 为 `null` 表示内容没有默认值. 也就是当源数据不存在时, 将直接跳过显示.
     */
    val defaultText: List<Component>?

    /**
     * Creates default lore line (if the [defaultText] is not `null`).
     */
    fun createDefault(): List<IndexedText>? {
        return defaultText?.let { listOf(SimpleIndexedText(sourceIndex, it)) }
    }

    companion object Shared {
        const val DEFAULT_IDENTIFIER = "default"
    }
}

/**
 * 用来描述在配置文件中就已预设好的 [IndexedText].
 *
 * 这些 [IndexedText] 的内容是预设的, 但最终是否渲染取决于物品(堆叠)本身的状态.
 * 使用场景包括但不限于在 `lore` 上面显示固定的一行空白, 或者显示一个固定的文本;
 * 如果物品堆叠本身不存在 `lore`, 则可以选择不渲染这一行空白文字.
 */
interface StaticTextMeta : TextMeta {
    /**
     * 该固定内容的文本.
     */
    val contents: List<Component>

    /**
     * 用于判断本内容是否应该被渲染.
     *
     * 如果 [companionNamespace] 所指定的内容出现在本内容的下面, 则说明本内容应该被渲染.
     *
     * 其中 [companionNamespace] 一共有3种不同的值：
     * - "*" 表示任意命名空间下的内容
     * - "`<namespace>`" 表示指定命名空间下的内容
     * - `null` 表示任何内容, 包括不存在任何内容的情况
     */
    val companionNamespace: String?

    override val sourceIndex: DerivedIndex
        // 经综合考虑, 固定内容的 SourceIndex 最好就是其在配置文件中原始索引的字符串形式
        // 例如, 这行固定内容位于列表中的第 3 个, 那么其 SourceIndex 就是 "fixed:3"
        // 这样刚好能保证不同的固定内容行都有唯一的 Index
        get() = Key.key(STATIC_IDENTIFIER, sourceOrdinal.toString())

    override fun generateIndexes(): List<DerivedIndex> {
        return listOf(sourceIndex)
    }

    override fun generateOrdinals(offset: Int): Map<DerivedIndex, DerivedOrdinal> {
        return mapOf(sourceIndex to sourceOrdinal + offset)
    }

    companion object Shared {
        const val STATIC_IDENTIFIER = "fixed"
    }
}

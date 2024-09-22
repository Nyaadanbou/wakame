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
     * 原始索引 (即配置文件的列表中, 未经衍生的字符串值).
     */
    val sourceTooltipIndex: DerivedTooltipIndex

    /**
     * 原始序数 (即配置文件的列表中, 所在的顺序位置).
     */
    val sourceTooltipOrdinal: DerivedTooltipOrdinal

    /**
     * 生成全部的 [DerivedTooltipIndex].
     *
     * ## 用途
     * [DerivedTooltipIndex] 将被直接用于查询 [IndexedText] 的顺序.
     *
     * ## 来源
     * [DerivedTooltipIndex] 是由 [SourceTooltipIndex] 经过一系列规则衍生出来的一个或多个值.
     *
     * ## 性质
     * 某些 [SourceTooltipIndex] 没有衍生规则, 因此这些 [SourceTooltipIndex] 在结果上与 [DerivedTooltipIndex]
     * 完全一致. 这种情况下 [DerivedTooltipIndex] 只是个单例列表. 而有些 [SourceTooltipIndex]
     * 存在衍生规则, 因此它们的 [SourceTooltipIndex] 就与 [DerivedTooltipIndex] 不一致.
     */
    fun generateTooltipIndexes(): List<DerivedTooltipIndex>

    /**
     * 生成从 [DerivedTooltipIndex] 到 [DerivedTooltipOrdinal] 的映射.
     *
     * 该映射将被直接用于查询 [IndexedText] 在物品提示框中的顺序.
     *
     * 该函数的参数 [offset] 为索引的偏移量. 你必须传入正确的 [offset], 否则该函数生成的 [DerivedTooltipOrdinal] 将是错的.
     *
     * 为了更好的理解 [offset] 的取值, 这里举个例子. 假设配置文件中有以下三行内容, 我们标记好各自的 [SourceTooltipOrdinal]:
     * - a ([SourceTooltipOrdinal] = 1)
     * - b ([SourceTooltipOrdinal] = 2)
     * - c ([SourceTooltipOrdinal] = 3)
     *
     * 经过衍生后的内容如下：
     * - a.1 ([SourceTooltipOrdinal] = 1)
     * - a.2 ([SourceTooltipOrdinal] = 1)
     * - b.1 ([SourceTooltipOrdinal] = 2)
     * - b.2 ([SourceTooltipOrdinal] = 2)
     * - c   ([SourceTooltipOrdinal] = 3)
     *
     * [DerivedTooltipOrdinal] 的计算方式为 [SourceTooltipOrdinal] + `local index` + `offset`. 其中 `local index` 是
     * [DerivedTooltipOrdinal] 的索引值. 假设 [offset] 为 0；现在基于 [SourceTooltipOrdinal], 为每个内容生成 [DerivedTooltipOrdinal]:
     * - a.1 ([SourceTooltipOrdinal] = 1, `offset` = 0 -> [DerivedTooltipOrdinal] = 1)
     * - a.2 ([SourceTooltipOrdinal] = 1, `offset` = 0 -> [DerivedTooltipOrdinal] = 2)
     * - b.1 ([SourceTooltipOrdinal] = 2, `offset` = 0 -> [DerivedTooltipOrdinal] = 2)
     * - b.2 ([SourceTooltipOrdinal] = 2, `offset` = 0 -> [DerivedTooltipOrdinal] = 3)
     * - c   ([SourceTooltipOrdinal] = 3, `offset` = 0 -> [DerivedTooltipOrdinal] = 3)
     *
     * 可以看到这里重复的 [DerivedTooltipOrdinal], 而这显然是错的. 导致该问题的原因是每一个内容的生成都假设它之前**不存在**衍生的内容.
     *
     * 为了解决这个问题, 我们引入 [offset] 的概念, 用来对生成的 [DerivedTooltipOrdinal] 进行偏移, 以实现整体上的正确性.
     *
     * 假设给定的 [offset] 都是正确的, 那么重新生成以上内容应该得到:
     * - a.1 ([SourceTooltipOrdinal] = 1, `offset` = 0 -> [DerivedTooltipOrdinal] = 1)
     * - a.2 ([SourceTooltipOrdinal] = 1, `offset` = 0 -> [DerivedTooltipOrdinal] = 2)
     * - b.1 ([SourceTooltipOrdinal] = 2, `offset` = 1 -> [DerivedTooltipOrdinal] = 3)
     * - b.2 ([SourceTooltipOrdinal] = 2, `offset` = 1 -> [DerivedTooltipOrdinal] = 4)
     * - c   ([SourceTooltipOrdinal] = 3, `offset` = 2 -> [DerivedTooltipOrdinal] = 5)
     *
     * @param offset [DerivedTooltipOrdinal] 的偏移量
     */
    fun generateTooltipOrdinals(offset: Int): Map<DerivedTooltipIndex, DerivedTooltipOrdinal>
}

/**
 * 代表一个标准的 [TextMeta].
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
    fun createDefault(): List<IndexedText>

    companion object {
        const val NAMESPACE_DEFAULT = "default"
    }
}

/**
 * 代表一个在配置文件中就已预设好的 [IndexedText].
 */
interface StaticTextMeta : TextMeta {
    /**
     * 该固定内容的文本.
     */
    val components: List<Component>

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

    override val sourceTooltipIndex: DerivedTooltipIndex
        // 经综合考虑, 固定内容的 RawTooltipIndex 最好就是其在配置文件中原始索引的字符串形式
        // 例如, 这行固定内容位于列表中的第 3 个, 那么其 RawTooltipIndex 就是 "fixed:3"
        // 这样刚好能保证不同的固定内容行都有唯一的 TooltipIndex
        get() = Key.key(NAMESPACE_CONSTANT, sourceTooltipIndex.toString())

    override fun generateTooltipIndexes(): List<DerivedTooltipIndex> {
        return listOf(sourceTooltipIndex)
    }

    override fun generateTooltipOrdinals(offset: Int): Map<DerivedTooltipIndex, DerivedTooltipOrdinal> {
        return mapOf(sourceTooltipIndex to sourceTooltipOrdinal + offset)
    }

    companion object {
        const val NAMESPACE_CONSTANT = "fixed"
    }
}

/**
 * 代表一个拥有自定义内容的 [StaticTextMeta].
 */
data class CustomStaticTextMeta(
    override val sourceTooltipOrdinal: SourceTooltipOrdinal,
    override val companionNamespace: String?,
    override val components: List<Component>,
) : StaticTextMeta

/**
 * 代表一个其内容为“空白”的 [StaticTextMeta].
 */
data class BlankStaticTextMeta(
    override val sourceTooltipOrdinal: SourceTooltipOrdinal,
    override val companionNamespace: String?,
) : StaticTextMeta {
    override val components: List<Component> = listOf(Component.empty())
}
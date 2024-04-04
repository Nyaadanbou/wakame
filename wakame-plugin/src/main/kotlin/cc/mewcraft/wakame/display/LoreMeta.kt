package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.text.Component

/**
 * 代表 Item Lore 中的顺序和相关信息。这是一个顶层接口。
 *
 * 注意与 [LoreLine] 区别 - [LoreMeta] 代表顺序和其他信息，而 [LoreLine] 仅代表内容。
 *
 * @see LoreLine
 * @see LoreMetaLookup
 */
internal sealed interface LoreMeta {
    /**
     * 在配置文件内的“原始 Key”（即配置文件的列表中，未经衍生的字符串值）。
     */
    val rawKey: RawKey

    /**
     * 在配置文件内的“原始 Index”（即配置文件中，列表元素的索引，从0开始）。
     */
    val rawIndex: RawIndex

    /**
     * 生成“完整 Key”（注意区别于“原始 Key”）。
     *
     * “完整 Key”将被直接用于查询 Lore Line 的顺序。
     *
     * [Full Keys][fullKeys] 是由 [Raw Key][rawKey] 经过一系列规则衍生出来的一个或多个键。
     *
     * 某些 [Raw Key][rawKey] 没有衍生规则，因此这些 [Raw Key][rawKey] 与
     * [Full Keys][fullKeys] 完全一致；这种情况下 [Full Keys][fullKeys]
     * 只是个单例列表。而有些 [Raw Key][rawKey] 存在衍生规则，因此它们的
     * [Raw Key][rawKey] 就与 [Full Keys][fullKeys] 不一致。
     */
    val fullKeys: List<FullKey> /* 必须按规定的按顺序排列 */

    /**
     * 生成“完整 Index”（注意区别于“原始 Index”）。
     *
     * “完整 Index”将被直接用于查询 Lore Line 的顺序。
     *
     * 该函数的参数 [offset] 为索引的偏移量。你必须传入正确的 [offset]，否则该函数生成的“完整 Index”将是错的。
     *
     * 为了更好的理解 [offset] 的取值，这里举个例子。假设配置文件中有以下三行内容，我们标记好各自的 Raw Index：
     * - a (Raw Index = 1)
     * - b (Raw Index = 2)
     * - c (Raw Index = 3)
     *
     * 经过衍生后的内容如下：
     * - a.1 (Raw Index = 1)
     * - a.2 (Raw Index = 1)
     * - b.1 (Raw Index = 2)
     * - b.2 (Raw Index = 2)
     * - c (Raw Index = 3)
     *
     * Full Index 的计算方式为 `Raw Index + Local Index + Offset`. 其中 Local Index 是
     * [fullKeys] 的索引值。假设 [offset] 为 0；现在基于 Raw Index，为每个内容生成 Full Index：
     * - a.1 (Raw Index = 1, Offset = 0 -> Full Index = 1)
     * - a.2 (Raw Index = 1, Offset = 0 -> Full Index = 2)
     * - b.1 (Raw Index = 2, Offset = 0 -> Full Index = 2)
     * - b.2 (Raw Index = 2, Offset = 0 -> Full Index = 3)
     * - c (Raw Index = 3, Offset = 0 -> Full Index = 3)
     *
     * 可以看到这里重复的 Full Index，而这显然是错的。导致该问题的原因是每一个内容的生成都假设它之前**不存在**衍生的内容。
     *
     * 为了解决这个问题，我们引入 [offset] 的概念，用来对生成的 Full Index 进行偏移，以实现整体上的正确性。
     *
     * 假设给定的 [offset] 都是正确的，那么重新生成以上内容应该得到：
     * - a.1 (Raw Index = 1, Offset = 0 -> Full Index = 1)
     * - a.2 (Raw Index = 1, Offset = 0 -> Full Index = 2)
     * - b.1 (Raw Index = 2, Offset = 1 -> Full Index = 3)
     * - b.2 (Raw Index = 2, Offset = 1 -> Full Index = 4)
     * - c (Raw Index = 3, Offset = 2 -> Full Index = 5)
     *
     * @param offset Full Index 的偏移量
     */
    fun fullIndexes(offset: Int): Map<FullKey, FullIndex> {
        val key2IndexMap = LinkedHashMap<FullKey, FullIndex>() // for debug inspection
        for ((localIndex, fullKey) in fullKeys.withIndex()) {
            key2IndexMap[fullKey] = rawIndex + localIndex + offset
        }
        return key2IndexMap
    }
}

/**
 * 代表**动态内容**的 [LoreMeta].
 *
 * 动态内容将在发包时根据物品的数据动态生成。
 *
 * @see MetaLoreMeta
 * @see SkillLoreMeta
 * @see AttributeLoreMeta
 */
internal sealed interface DynamicLoreMeta : LoreMeta {
    /**
     * 内容的默认值。如果源数据不存在，将渲染默认值，而不是跳过渲染。
     *
     * 为 `null` 表示内容没有默认值，也就是当源数据不存在时，将跳过渲染。
     */
    val default: List<Component>?
}

/**
 * 代表**固定内容**的 [LoreMeta].
 *
 * 固定内容在配置文件中就早已经定义好。
 *
 * @see CustomFixedLoreMeta
 * @see EmptyFixedLoreMeta
 */
internal sealed interface FixedLoreMeta : LoreMeta {
    /**
     * 该固定内容的文本。
     */
    val components: List<Component>

    /**
     * 用于判断本内容是否应该被渲染。
     *
     * 如果 [companionNamespace] 所指定的内容出现在本内容的下面，则说明本内容应该被渲染。
     *
     * 其中 [companionNamespace] 一共有3种不同的值：
     * - "*" 表示任意命名空间下的内容
     * - "`<namespace>`" 表示指定命名空间下的内容
     * - `null` 表示任何内容，包括不存在任何内容的情况
     */
    val companionNamespace: String? /* = "*"｜"<namespace>"｜null */

    override val rawKey: RawKey
        // 经综合考虑，固定内容的 Raw Key 最好就是其在配置文件中“原始顺序”的字符串形式
        // 例如，这行固定内容位于列表中的第 3 个，那么其 Raw Key 就是 "fixed:3"
        // 这样刚好能保证不同的固定内容行都有唯一的 Full Key
        get() = Key("fixed", rawIndex.toString())

    override val fullKeys: List<FullKey>
        get() = listOf(rawKey)

    override fun fullIndexes(offset: Int): Map<FullKey, FullIndex> {
        return mapOf(rawKey to rawIndex + offset)
    }
}
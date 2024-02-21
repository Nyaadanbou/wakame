package cc.mewcraft.wakame.display

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
     *
     * @see fullKeys
     */
    val rawKey: RawKey

    /**
     * 在配置文件内的“原始 Index”（即配置文件中，列表元素的索引，从0开始）。
     *
     * @see fullIndexes
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
     *
     * @see rawKey
     */
    val fullKeys: List<FullKey> /* 必须规定的按顺序排列 */

    /**
     * 生成“完整 Index”（注意区别于“原始 Index”）。
     *
     * “完整 Index”将被直接用于查询 Lore Line 的顺序。
     */
    val fullIndexes: Map<FullKey, FullIndex>
        get() {
            val ret = HashMap<FullKey, FullIndex>()
            for ((localIndex, fullKey) in fullKeys.withIndex()) {
                ret[fullKey] = rawIndex + localIndex
            }
            return ret
        }
}

/**
 * 代表**动态内容**的 [LoreMeta].
 *
 * 动态内容将在发包时根据物品的数据动态生成。
 *
 * @see MetaLoreMeta
 * @see AbilityLoreMeta
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
        // 经综合考虑，固定内容的 raw key 最好就是其在配置文件中“原始顺序”的字符串形式
        // 例如，这行固定内容位于列表中的第3个，那么其 raw key 就是 "fixed:3"
        // 同理，下面的 fullKeys 和 fullIndexes 也遵循该逻辑
        get() = RawKey.key("fixed", rawIndex.toString())

    override val fullKeys: List<FullKey>
        get() = listOf(rawKey)

    override val fullIndexes: Map<FullKey, FullIndex>
        get() = mapOf(rawKey to rawIndex)
}
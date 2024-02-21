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
     * 在配置文件内的原始 Key（即在配置文件中未经衍生的字符串值）。
     */
    val rawKey: RawKey

    /**
     * 在配置文件内的原始 Index（即在配置文件中的列表的索引，从0开始）。
     */
    val rawIndex: RawIndex

    /**
     * 生成所有的“完整 Key”（注意区别于“原始 Key”）。**完整 Key 将用于最终的顺序查询。**
     *
     * [Full Key List][computeFullKeys] 是由单个 [Raw Key][rawKey]
     * 经过一系列规则衍生出来的一个或多个 Full Key。某些内容的 Raw Key 没有衍生规则，因此这些内容的 Raw Key 与
     * Full Key 完全一致。而有些内容的 Raw Key 有衍生规则，因此它们的 Raw Key 就与 Full Key 不一致。
     */
    fun computeFullKeys(): List<FullKey>
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
    override val rawKey: RawKey
        // 经综合考虑，固定内容的 raw key 最好就是其在配置文件中“原始顺序”的字符串形式
        // 例如，这行固定内容位于列表中的第3个，那么其 raw key 就是 "fixed:3"
        get() = RawKey.key("fixed", rawIndex.toString())

    /**
     * 用于判断本内容是否应该被渲染。如果 [companionNamespace] 出现在本内容的**下面**，则说明本内容应该被渲染。
     *
     * [companionNamespace] 一共有3种不同的值：
     * - "*" 表示任意命名空间下的内容
     * - "`<namespace>`" 表示指定命名空间下的内容
     * - `null` 表示任何内容，包括不存在任何内容的情况
     */
    val companionNamespace: String? /* = "*"｜"<namespace>"｜null */

    override fun computeFullKeys(): List<FullKey> {
        // 首先要知道 full key 是用于最终的顺序查询
        // 而逻辑上，固定内容的 full key 只需要与 raw key 一致
        // 就可以达到我们的目的 - 用于最终的顺序查询
        return listOf(rawKey)
    }
}
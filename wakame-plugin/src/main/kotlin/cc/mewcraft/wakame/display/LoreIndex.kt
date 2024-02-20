package cc.mewcraft.wakame.display

/**
 * 代表 Item Lore 中的任意内容的顺序。
 *
 * 注意与 [LoreLine] 区别 - [LoreIndex] 代表顺序，而 [LoreLine] 代表内容。
 *
 * @see LoreLine
 * @see LoreIndexLookup
 */
internal sealed interface LoreIndex {
    /**
     * 在配置文件内的原始 Key（即在配置文件内未经衍生处理的字符串值）。
     */
    val rawKey: RawKey

    /**
     * 在配置文件内的原始 Index（即在配置文件中的 List 的索引，从0开始）。
     */
    val rawIndex: RawIndex

    /**
     * 生成所有的“完整 Key”（注意区别于“原始 Key”）。完整 Key 将用于最终的顺序查询。
     *
     * [Full Key List][computeFullKeys] 是由单个 [Raw Key][rawKey]
     * 经过一系列规则衍生出来的一个或多个 Full Key。某些内容的 Raw Key 没有衍生规则，因此这些内容的 Raw Key 与
     * Full Key 完全一致。而有些内容的 Raw Key 有衍生规则，因此它们的 Raw Key 就与 Full Key 不一致。
     */
    fun computeFullKeys(): List<FullKey>
}

/**
 * 代表 Item Lore 中的固定内容的顺序。
 *
 * @see CustomFixedLoreIndex
 * @see EmptyFixedLoreIndex
 */
internal sealed interface FixedLoreIndex : LoreIndex {
    override val rawKey: RawKey
        // 实际上，固定内容的 raw key 就是其在配置文件中“原始顺序”的字符串形式
        // 例如，这行固定内容位于列表中的第3个，那么其 raw key 就是 "fixed:3"
        get() = RawKey.key("fixed", rawIndex.toString())

    override val rawIndex: RawIndex

    override fun computeFullKeys(): List<FullKey> {
        // 首先要知道 full key 是用于最终的顺序查询
        // 而逻辑上，固定内容的 full key 只需要与 raw key 一致
        // 就可以达到我们的最终的目的 - 用于最终的顺序查询
        return listOf(rawKey)
    }
}
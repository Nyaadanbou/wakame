package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.text.Component

/**
 * 这是一个顶层接口.
 *
 * 代表 [Item Lore](https://minecraft.wiki/w/Data_component_format#lore) 中的顺序, 以及相关信息.
 *
 * 注意与 [LoreLine] 区别 - [LoreMeta] 描述的是顺序及其他信息, 而 [LoreLine] 仅代表内容.
 *
 * @see LoreLine
 * @see LoreMetaLookup
 */
internal interface LoreMeta {
    /**
     * 在配置文件内的 [RawTooltipKey] (即配置文件的列表中, 未经衍生的字符串值).
     */
    val rawTooltipKey: RawTooltipKey

    /**
     * 在配置文件内的 [RawTooltipIndex] (即配置文件中, 列表元素的索引, 从0开始).
     */
    val rawTooltipIndex: RawTooltipIndex

    /**
     * 生成全部的 [TooltipIndex].
     *
     * ## 用途
     * [TooltipIndex] 将被直接用于查询 [LoreLine] 的顺序.
     *
     * ## 来源
     * [TooltipIndex] 是由 [RawTooltipKey] 经过一系列规则衍生出来的一个或多个值.
     *
     * ## 性质
     * 某些 [RawTooltipKey] 没有衍生规则, 因此这些 [RawTooltipKey] 在结果上与 [TooltipIndex]
     * 完全一致. 这种情况下 [TooltipIndex] 只是个单例列表. 而有些 [RawTooltipKey]
     * 存在衍生规则, 因此它们的 [RawTooltipKey] 就与 [TooltipIndex] 不一致.
     */
    fun generateTooltipKeys(): List<TooltipKey> /* 必须按规定的按顺序排列 */

    /**
     * 生成从 [TooltipKey] 到 [TooltipIndex] 的映射.
     *
     * 该映射将被直接用于查询 [LoreLine] 在物品提示框中的顺序.
     *
     * 该函数的参数 [offset] 为索引的偏移量. 你必须传入正确的 [offset], 否则该函数生成的 [TooltipIndex] 将是错的.
     *
     * 为了更好的理解 [offset] 的取值, 这里举个例子. 假设配置文件中有以下三行内容, 我们标记好各自的 [RawTooltipIndex]:
     * - a ([RawTooltipIndex] = 1)
     * - b ([RawTooltipIndex] = 2)
     * - c ([RawTooltipIndex] = 3)
     *
     * 经过衍生后的内容如下：
     * - a.1 ([RawTooltipIndex] = 1)
     * - a.2 ([RawTooltipIndex] = 1)
     * - b.1 ([RawTooltipIndex] = 2)
     * - b.2 ([RawTooltipIndex] = 2)
     * - c ([RawTooltipIndex] = 3)
     *
     * [TooltipIndex] 的计算方式为 [RawTooltipIndex] + `local index` + `offset`. 其中 `local index` 是
     * [TooltipIndex] 的索引值. 假设 [offset] 为 0；现在基于 [RawTooltipIndex], 为每个内容生成 [TooltipIndex]:
     * - a.1 ([RawTooltipIndex] = 1, `offset` = 0 -> [TooltipIndex] = 1)
     * - a.2 ([RawTooltipIndex] = 1, `offset` = 0 -> [TooltipIndex] = 2)
     * - b.1 ([RawTooltipIndex] = 2, `offset` = 0 -> [TooltipIndex] = 2)
     * - b.2 ([RawTooltipIndex] = 2, `offset` = 0 -> [TooltipIndex] = 3)
     * - c ([RawTooltipIndex] = 3, `offset` = 0 -> [TooltipIndex] = 3)
     *
     * 可以看到这里重复的 [TooltipIndex], 而这显然是错的. 导致该问题的原因是每一个内容的生成都假设它之前**不存在**衍生的内容.
     *
     * 为了解决这个问题, 我们引入 [offset] 的概念, 用来对生成的 [TooltipIndex] 进行偏移, 以实现整体上的正确性.
     *
     * 假设给定的 [offset] 都是正确的, 那么重新生成以上内容应该得到:
     * - a.1 ([RawTooltipIndex] = 1, `offset` = 0 -> [TooltipIndex] = 1)
     * - a.2 ([RawTooltipIndex] = 1, `offset` = 0 -> [TooltipIndex] = 2)
     * - b.1 ([RawTooltipIndex] = 2, `offset` = 1 -> [TooltipIndex] = 3)
     * - b.2 ([RawTooltipIndex] = 2, `offset` = 1 -> [TooltipIndex] = 4)
     * - c ([RawTooltipIndex] = 3, `offset` = 2 -> [TooltipIndex] = 5)
     *
     * @param offset [TooltipIndex] 的偏移量
     */
    fun generateTooltipKeyIndexes(offset: Int): Map<TooltipKey, TooltipIndex> {
        val key2IndexMap = LinkedHashMap<TooltipKey, TooltipIndex>() // for debug inspection
        for ((localIndex, fullKey) in generateTooltipKeys().withIndex()) {
            key2IndexMap[fullKey] = rawTooltipIndex + localIndex + offset
        }
        return key2IndexMap
    }
}

/**
 * 代表内容动态变化的 [LoreMeta].
 *
 * 动态内容将根据物品的具体数据动态生成.
 */
internal interface DynamicLoreMeta : LoreMeta {
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
    fun createDefault(): List<LoreLine>?
}

/**
 * 代表内容始终不变的 [LoreMeta].
 *
 * 固定内容在配置文件中就早已经定义好.
 *
 * @see CustomConstantLoreMeta
 * @see BlankConstantLoreMeta
 */
internal interface ConstantLoreMeta : LoreMeta {
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
    val companionNamespace: String? /* = "*"｜"<namespace>"｜null */

    override val rawTooltipKey: RawTooltipKey
        // 经综合考虑, 固定内容的 RawTooltipKey 最好就是其在配置文件中“原始顺序”的字符串形式
        // 例如, 这行固定内容位于列表中的第 3 个, 那么其 RawTooltipKey 就是 "fixed:3"
        // 这样刚好能保证不同的固定内容行都有唯一的 TooltipKey
        get() = Key("fixed", rawTooltipIndex.toString())

    override fun generateTooltipKeys(): List<TooltipKey> {
        return listOf(rawTooltipKey)
    }

    override fun generateTooltipKeyIndexes(offset: Int): Map<TooltipKey, TooltipIndex> {
        return mapOf(rawTooltipKey to rawTooltipIndex + offset)
    }
}

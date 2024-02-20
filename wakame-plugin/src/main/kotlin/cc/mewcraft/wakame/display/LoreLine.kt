package cc.mewcraft.wakame.display

import net.kyori.adventure.text.Component

/**
 * 代表 Item Lore 中的任意内容。
 *
 * 注意与 [LoreIndex] 区别 - [LoreIndex] 代表顺序，而 [LoreLine] 代表内容。
 *
 * @see LoreIndex
 * @see LoreIndexLookup
 */
internal sealed interface LoreLine {
    /**
     * 能够唯一识别这一行的标识。
     */
    val key: FullKey

    /**
     * 最终生成的文本内容。如果有多行则该列表就有多个元素。
     */
    val lines: List<Component>
}

/**
 * 代表一个任意的固定内容。
 */
internal interface FixedLoreLine : LoreLine

/**
 * 代表一个描述元数据的内容。
 */
internal interface MetaLoreLine : LoreLine

/**
 * 代表一个描述属性的内容。
 */
internal interface AttributeLoreLine : LoreLine

/**
 * 代表一个描述技能的内容。
 */
internal interface AbilityLoreLine : LoreLine

package cc.mewcraft.wakame.display

import net.kyori.adventure.text.Component

/**
 * 代表 Item Lore 中的任意内容。
 *
 * 注意与 [LoreLine] 区别 - [LoreMeta] 代表顺序及其他信息，而 [LoreLine] 仅代表内容。
 *
 * @see LoreMeta
 * @see LoreMetaLookup
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
internal interface FixedLine : LoreLine

/**
 * 代表一个描述元数据的内容。
 */
internal interface ItemMetaLine : LoreLine

/**
 * 代表一个描述属性的内容。
 */
internal interface AttributeLine : LoreLine

/**
 * 代表一个描述技能的内容。
 */
internal interface SkillLine : LoreLine

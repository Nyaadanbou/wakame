package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.GenericKeys
import net.kyori.adventure.text.Component

/**
 * 代表 [Item Lore](https://minecraft.wiki/w/Data_component_format#lore) 中的内容。
 *
 * 注意与 [LoreMeta] 区别 - [LoreMeta] 描述的是顺序及其他信息，而 [LoreLine] 仅代表内容。
 *
 * @see LoreMeta
 * @see LoreMetaLookup
 */
interface LoreLine {
    /**
     * 能够唯一识别这一内容的标识。
     */
    val key: FullKey

    /**
     * 最终生成的文本内容。如果有多行则该列表就有多个元素。
     */
    val lines: List<Component>
}

/**
 * 代表一个不会显示的内容。
 */
data object NoopLoreLine : LoreLine {
    override val key: FullKey = GenericKeys.NOOP
    override val lines: List<Component> = emptyList()
    override fun toString(): String = "NOOP"
}

/**
 * 代表一个始终不变的内容（位置也不变）。
 */
data class ConstantLoreLine(
    override val key: FullKey,
    override val lines: List<Component>,
) : LoreLine

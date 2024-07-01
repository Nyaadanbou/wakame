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
    val key: TooltipKey

    /**
     * 最终生成的文本内容。如果有多行则该列表就有多个元素。
     */
    val content: List<Component>

    /**
     * 该内容是否应该显示。
     */
    val shouldShow: Boolean
        get() = this is NoopLoreLine

    /**
     * 该内容是否是始终不变的。
     */
    val isConstant: Boolean
        get() = this is ConstantLoreLine

    /**
     * This companion object provides constructor functions of [LoreLine].
     */
    companion object {
        fun noop(): LoreLine {
            return NoopLoreLine
        }

        fun constant(key: TooltipKey, lines: List<Component>): LoreLine {
            return ConstantLoreLine(key, lines)
        }

        fun simple(key: TooltipKey, lines: List<Component>): LoreLine {
            return SimpleLoreLine(key, lines)
        }

        fun bulk(map: Map<TooltipKey, LoreLine>): List<LoreLine> {
            TODO("改进 LoreLine 接口, 使得 LoreLine 可以包含多个")
        }
    }
}

/**
 * 代表一个不会显示的内容。
 */
private data object NoopLoreLine : LoreLine {
    override val key: TooltipKey = GenericKeys.NOOP
    override val content: List<Component> = emptyList()
    override fun toString(): String = "NOOP"
}

/**
 * 代表一个始终不变的内容（位置也不变）。
 */
private data class ConstantLoreLine(
    override val key: TooltipKey,
    override val content: List<Component>,
) : LoreLine

/**
 * 代表一个一般的内容，例如属性，技能等。
 */
private data class SimpleLoreLine(
    override val key: TooltipKey,
    override val content: List<Component>,
) : LoreLine
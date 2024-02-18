package cc.mewcraft.wakame.display

import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

/**
 * 代表 lore 中的一行。
 */
internal sealed interface LoreLine {
    /**
     * 能够唯一识别这一行的标识。
     */
    val key: Key // TODO 改用 net.kyori.adventure.key.Key

    /**
     * 最终生成的文本内容。
     */
    val line: List<Component>
}

/**
 * 代表 lore 中固定内容的一行。
 */
internal interface FixedLoreLine : LoreLine {
    /**
     * 这里的 [key] 就是固定内容本身。
     */
    override val key: Key
}

/**
 * 代表 lore 中关于元数据的一行。
 */
internal interface MetaLoreLine : LoreLine

/**
 * 代表 lore 中关于属性的一行。
 */
internal interface AttributeLoreLine : LoreLine

/**
 * 代表 lore 中关于技能的一行。
 */
internal interface AbilityLoreLine : LoreLine

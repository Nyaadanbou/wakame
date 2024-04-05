package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.adventure.Keyed
import cc.mewcraft.wakame.item.binary.NekoStack
import net.kyori.adventure.key.Key

/*

   该文件定义了词条栏和与之相关的所有顶层接口。

*/

/**
 * A [cell][Cell] is conceptually a container which contains zero or more [cores][Core].
 */
interface Cell {
    /**
     * The ID of this cell.
     *
     * No cells with identical ID on a single item.
     */
    val id: String
}

/**
 * A [core][Core] is a functional thing stored in a [cell][Cell].
 */
interface Core : Keyed {
    /**
     * The key of the core, which must be unique among all other [cores][Core].
     */
    override val key: Key
}

/**
 * Represents a condition testing whether a [cell][Cell] in a
 * [NekoStack] should be enabled or not. If a [cell][Cell] is
 * not enabled, it won't provide any effects as if it doesn't exist.
 */
interface Curse : Keyed {
    /**
     * The key of this lock condition. Used to identify the condition in the
     * context of binary and schema item.
     */
    override val key: Key
}


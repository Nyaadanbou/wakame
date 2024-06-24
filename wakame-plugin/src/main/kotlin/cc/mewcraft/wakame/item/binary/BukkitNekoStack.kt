package cc.mewcraft.wakame.item.binary

import cc.mewcraft.wakame.item.binary.show.CustomDataAccessor

interface BukkitNekoStack : NekoStack {
    /**
     * Gets a [PlayNekoStack] representation.
     */
    val play: PlayNekoStack

    /**
     * Gets a [ShowNekoStack] representation.
     */
    val show: ShowNekoStack
}

/**
 * Represents a [NekoStack] which will ultimately be used by players.
 */
interface PlayNekoStack : BukkitNekoStack

/**
 * Represents a [NekoStack] which is **solely** used to build showcases.
 *
 * A [ShowNekoStack] differs from [PlayNekoStack] in that it provides interfaces
 * to conveniently modify the name and lore of the [itemStack].
 *
 * For example, a [ShowNekoStack] may:
 * - be stored in a display item
 * - be placed in a virtual inventory
 */
interface ShowNekoStack : BukkitNekoStack {
    /**
     * The [CustomDataAccessor] of this item.
     */
    val customData: CustomDataAccessor
}

package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.item.Cell
import cc.mewcraft.wakame.item.ShadowTagLike
import cc.mewcraft.wakame.item.binary.core.BinaryCore
import cc.mewcraft.wakame.item.binary.curse.BinaryCurse
import cc.mewcraft.wakame.item.scheme.cell.SchemeCell

/**
 * Represents a certain state from a [SchemeCell].
 *
 * The implementation should all be **immutable**.
 *
 * It's much like a POJO, and it's intended to serve as a bridge to
 * manipulate the underlying NBT structure of an item.
 */
interface BinaryCell : Cell, ShadowTagLike {
    /**
     * Returns `true` if the cell is reforgeable.
     */
    val canReforge: Boolean

    /**
     * Returns `true` if the cell is allowed to be modified by players.
     */
    val canOverride: Boolean

    /**
     * The binary core of the cell.
     */
    val binaryCore: BinaryCore

    /**
     * The binary lock condition of the cell.
     */
    val binaryCurse: BinaryCurse

    /**
     * The reforge metadata of the cell.
     */
    val reforgeMeta: ReforgeMeta
}

fun emptyBinaryCell(): BinaryCell =
    EmptyBinaryCell
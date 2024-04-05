package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.item.Cell
import cc.mewcraft.wakame.item.ShadowTagLike
import cc.mewcraft.wakame.item.binary.cell.core.BinaryCore
import cc.mewcraft.wakame.item.binary.cell.curse.BinaryCurse
import cc.mewcraft.wakame.item.schema.cell.SchemaCell

/**
 * Represents a certain state from a [SchemaCell].
 *
 * The implementation should all be **immutable**.
 *
 * It's much like a POJO, and it's intended to serve as a bridge to
 * manipulate the underlying NBT structure of an item.
 */
interface BinaryCell : Cell, ShadowTagLike {
    /**
     * Returns `true` if this cell is reforgeable.
     */
    val isReforgeable: Boolean

    /**
     * Returns `true` if this cell is allowed to be modified by players.
     */
    val isOverridable: Boolean

    /**
     * The core stored in this cell.
     */
    val core: BinaryCore

    /**
     * The curse stored in this cell.
     */
    val curse: BinaryCurse

    /**
     * The reforge data stored in this cell.
     */
    val reforgeData: ReforgeData
}

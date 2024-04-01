package cc.mewcraft.wakame.item.schema.cell

import cc.mewcraft.wakame.item.Cell
import cc.mewcraft.wakame.item.binary.cell.BinaryCell
import cc.mewcraft.wakame.item.binary.cell.core.BinaryCore
import cc.mewcraft.wakame.item.binary.cell.curse.BinaryCurse
import cc.mewcraft.wakame.item.schema.SchemaGenerationContext
import cc.mewcraft.wakame.item.schema.cell.core.SchemaCore
import cc.mewcraft.wakame.item.schema.cell.curse.SchemaCurse
import cc.mewcraft.wakame.random.Group
import cc.mewcraft.wakame.random.Pool

typealias SchemaCorePool = Pool<SchemaCore, SchemaGenerationContext>
typealias SchemaCoreGroup = Group<SchemaCore, SchemaGenerationContext>

typealias SchemaCursePool = Pool<SchemaCurse, SchemaGenerationContext>
typealias SchemaCurseGroup = Group<SchemaCurse, SchemaGenerationContext>

/**
 * Represents all possible states that may put into a [BinaryCell]. There
 * are two types of data in a [schema cell][SchemaCell]: invariant data and
 * variant data.
 *
 * ## Invariant data
 *
 * Invariant data should be put into a [binary cell][BinaryCell] "as-is".
 *
 * ## Variant data
 *
 * Variant data are intended to be randomized. That is, you should generate
 * a random [core][BinaryCore] and a random [curse][BinaryCurse], using
 * [coreSelector] and [curseSelector] respectively. Then, you use the
 * randomly generated data to create a [binary cell][BinaryCell].
 */
interface SchemaCell : Cell {
    /**
     * Returns `true` if the cell is configured as "reforgeable".
     */
    val canReforge: Boolean

    /**
     * Returns `true` if the cell is configured as "overridable".
     */
    val canOverride: Boolean

    /**
     * Returns `true` if the cell should be removed from the item stack when
     * nothing is picked from the [coreSelector].
     */
    val keepEmpty: Boolean

    /**
     * The [group][Group] of [cores][SchemaCore] owned by the cell.
     */
    val coreSelector: SchemaCoreGroup

    /**
     * The [group][Group] of [curses][SchemaCurse] owned by ths cell.
     */
    val curseSelector: SchemaCurseGroup
}

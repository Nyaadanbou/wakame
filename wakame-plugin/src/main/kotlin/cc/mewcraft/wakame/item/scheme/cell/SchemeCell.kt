package cc.mewcraft.wakame.item.scheme.cell

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.item.Cell
import cc.mewcraft.wakame.item.binary.cell.BinaryCell
import cc.mewcraft.wakame.item.binary.core.BinaryCore
import cc.mewcraft.wakame.item.binary.curse.BinaryCurse
import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.item.scheme.core.SchemeCore
import cc.mewcraft.wakame.item.scheme.curse.SchemeCurse
import cc.mewcraft.wakame.random.Group
import cc.mewcraft.wakame.random.Pool

typealias SchemeCorePool = Pool<SchemeCore, SchemeGenerationContext>
typealias SchemeCoreGroup = Group<SchemeCore, SchemeGenerationContext>

typealias SchemeCursePool = Pool<SchemeCurse, SchemeGenerationContext>
typealias SchemeCurseGroup = Group<SchemeCurse, SchemeGenerationContext>

/**
 * Represents all possible states that may put into a [BinaryCell]. There
 * are two types of data in a [scheme cell][SchemeCell]: invariant data and
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
interface SchemeCell : Cell {

    // region Invariant Data
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
    // endregion

    // region Variant Data
    /**
     * The [group][Group] of [cores][SchemeCore] owned by the cell.
     */
    val coreSelector: SchemeCoreGroup

    /**
     * The [group][Group] of [curses][SchemeCurse] owned by ths cell.
     */
    val curseSelector: SchemeCurseGroup
    // endregion
}

/**
 * Gets the empty cell.
 */
@OptIn(InternalApi::class)
fun emptySchemeCell(): SchemeCell = EmptySchemeCell
package cc.mewcraft.wakame.item.schema.cell

import cc.mewcraft.wakame.item.Cell
import cc.mewcraft.wakame.item.binary.cell.BinaryCell
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
 * Represents all possible states of a [BinaryCell].
 */
interface SchemaCell : Cell {

    val createOptions: CreateOptions

    val modifyOptions: ModifyOptions

    /**
     * Options applied upon the cell being generated.
     */
    interface CreateOptions {
        /**
         * The [group][Group] of [cores][SchemaCore] owned by the cell.
         */
        val core: SchemaCoreGroup

        /**
         * The [group][Group] of [curses][SchemaCurse] owned by ths cell.
         */
        val curse: SchemaCurseGroup
    }

    /**
     * Options applied upon the cell being reforged.
     */
    interface ModifyOptions {
        /**
         * Returns `true` if the cell is configured as "reforgeable".
         */
        val isReforgeable: Boolean
    }
}

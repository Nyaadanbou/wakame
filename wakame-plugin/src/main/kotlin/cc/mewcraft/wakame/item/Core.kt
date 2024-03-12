package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.adventure.Keyed
import net.kyori.adventure.key.Key

/**
 * A [core][Core] is a functional thing in a [cell][Cell].
 *
 * @see Cell
 */
interface Core : Keyed {
    /**
     * The key of the core, which must be unique among all other [cores][Core].
     */
    override val key: Key
}

/**
 * Represents the data of a [core][Core].
 */
sealed interface CoreData

/**
 * Represents the data of a plain [core][Core].
 */
interface BinaryCoreData : CoreData

/**
 * Represents the data of a schema [core][Core].
 */
interface SchemaCoreData : CoreData

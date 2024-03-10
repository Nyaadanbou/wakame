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

/*
   These are top-level interfaces.

   You should not directly implement them.

   Instead, you create your own interfaces extending them,
   so that you can get rid of the generic parameters
   in your implementations and call sites.
*/

/**
 * Data flow: [I] -> [SchemaCoreData]
 */
interface SchemaCoreDataBuilder<in I, out O : SchemaCoreData> {
    fun build(source: I): O
}

/**
 * Data flow: [SchemaCoreData] -> [BinaryCoreData]
 */
interface SchemaCoreDataBaker<in I : SchemaCoreData, out O : BinaryCoreData> {
    fun bake(schema: I, factor: Int): O
}

/**
 * Data flow: [BinaryCoreData] -> [O]
 */
interface NbtCoreDataEncoder<I : BinaryCoreData, out O> {
    fun encode(value: I): O
}

/**
 * Data flow: [I] -> [BinaryCoreData]
 */
interface NbtCoreDataDecoder<I, out O : BinaryCoreData> {
    fun decode(value: I): O
}
package cc.mewcraft.wakame.item

/**
 * Represents the value of a [Core]. The value can be a scalar, but it's
 * not limited to just be a scalar. It may be a compound value; that is, a
 * set of different values of different types.
 *
 * See the subclasses for more details.
 */
interface CoreValue

/**
 * The value representation in NBT.
 */
interface BinaryCoreValue : CoreValue

/**
 * The value representation in scheme.
 */
interface SchemeCoreValue : CoreValue
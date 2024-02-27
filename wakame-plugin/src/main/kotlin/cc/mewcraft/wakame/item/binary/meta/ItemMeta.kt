package cc.mewcraft.wakame.item.binary.meta

import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed

/**
 * An abstraction layer of an item meta. This interface defines
 * basic read and write operations for the underlying item NBT.
 *
 * @param T the value stored in this [ItemMeta]
 * @constructor the primary constructor must have
 * a single parameter of type [ItemMetaHolder]
 */
sealed interface ItemMeta<T> : Keyed {

    /**
     * The key of this [ItemMeta].
     */
    val key: Key

    // Overrides Java's getter
    override fun key(): Key = key

    /**
     * Gets the companion object.
     *
     * **Caution: To correctly implement this function, you must declare a companion
     * object that implements [ItemMetaCompanion] and make this function return
     * that companion object.**
     */
    val companion: ItemMetaCompanion

    /**
     * Gets the value of this item meta.
     *
     * If you have already called [remove], that means the underlying data has
     * been removed from the NBT, then calling this function will definitely
     * throw exception.
     *
     * @throws IllegalStateException
     */
    fun get(): T = checkNotNull(getOrNull()) { "Can't find tag for meta '$key'" }

    /**
     * Gets the value of this item meta or null.
     */
    fun getOrNull(): T?

    /**
     * Sets the value of this item meta.
     * This will write data into the underlying NBT.
     */
    fun set(value: T)

    /**
     * Remove this item meta.
     * This will remove data from the underlying NBT.
     */
    fun remove()

}

interface ItemMetaCompanion {
    /**
     * Checks if this item meta exists in the given compound.
     * Returns `true` if the compound contains the ItemMeta.
     *
     * @param compound the compound that may include the ItemMeta
     */
    fun contains(compound: CompoundShadowTag): Boolean
}
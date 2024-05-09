package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.display.DisplayLoreProvider
import cc.mewcraft.wakame.item.ItemMeta

/**
 * Represents a data accessor of an item meta on the ItemStack.
 *
 * This is an interface to read/write/remove the item meta for the ItemStack.
 *
 * @param T the value "stored" in this [BinaryItemMeta]
 * @constructor the primary constructor must have a single parameter of type [ItemMetaAccessor]
 */
sealed interface BinaryItemMeta<T> : ItemMeta, DisplayLoreProvider {

    /**
     * Checks whether the item meta exists in the underlying data.
     *
     * If [BinaryItemMeta.exists] returns `true`, it is guaranteed that
     * [BinaryItemMeta.get] will return without throwing an exception.
     */
    val exists: Boolean

    /**
     * Gets the value of this item meta.
     *
     * If you have already called [BinaryItemMeta.remove], that means the
     * underlying data has been removed from the NBT, then calling this
     * function will definitely throw exception.
     *
     * @throws IllegalStateException
     */
    fun get(): T = checkNotNull(getOrNull()) { "Can't find tag for meta '$key'" }

    /**
     * Gets the value of this item meta or `null`, if the underlying NBT
     * data does not exist.
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

/**
 * Sets the value of this item meta.
 *
 * **This function accepts any type of value to avoid writing nagging source code, but it will
 * still do the type check at the runtime!**
 *
 * @throws IllegalArgumentException if the given [value] is not compatible with this item meta
 */
fun <T> BinaryItemMeta<T>.set(value: Any?) {
    requireNotNull(value) { "Can't set null value for meta: ${this::class.simpleName}" }

    @Suppress("UNCHECKED_CAST")
    val maybe = value as? T
    if (maybe != null) {
        this.set(maybe)
    } else {
        throw IllegalArgumentException(
            "The given value (type: ${value::class.simpleName}) is not compatible with this meta: ${this::class.simpleName}"
        )
    }
}

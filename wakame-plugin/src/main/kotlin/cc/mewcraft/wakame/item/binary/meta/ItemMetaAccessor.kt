package cc.mewcraft.wakame.item.binary.meta

import kotlin.reflect.KClass

/**
 * This is an interface to get specific [item meta accessor][BinaryItemMeta] for the ItemStack.
 */
interface ItemMetaAccessor {

    /**
     * Gets a **snapshot** set which, at the time you called this function,
     * contains all the **existing** [BinaryItemMeta] on the item, which means that it is safe
     * to call [BinaryItemMeta.get] on every element in the set without throwing an exception
     * (except that you have called [BinaryItemMeta.remove] before [BinaryItemMeta.get]).
     */
    val snapshot: Set<BinaryItemMeta<*>>

    /**
     * Gets the accessor of specific binary item meta.
     *
     * If the meta does not exist in the item, this will return `null`.
     *
     * @param M the binary item meta type
     * @param clazz the binary item meta clazz
     * @return the binary item meta instance
     */
    fun <M : BinaryItemMeta<*>> getAccessor(clazz: KClass<out M>): M?

    /**
     * Gets the accessor of specific binary item meta or create it, if it does not exist.
     *
     * @param M the binary item meta type
     * @param clazz the binary item meta clazz
     * @return the binary item meta instance
     */
    fun <M : BinaryItemMeta<*>> getAccessorOrCreate(clazz: KClass<out M>): M

}

/**
 * @see ItemMetaAccessor.getAccessor
 */
inline fun <reified M : BinaryItemMeta<*>> ItemMetaAccessor.getAccessor(): M? {
    return this.getAccessor(M::class)
}

/**
 * @see ItemMetaAccessor.getAccessorOrCreate
 */
inline fun <reified M : BinaryItemMeta<*>> ItemMetaAccessor.getAccessorOrCreate(): M {
    return this.getAccessorOrCreate(M::class)
}
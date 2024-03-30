package cc.mewcraft.wakame.item.binary.meta

import kotlin.reflect.KClass

/**
 * This is an interface to get the [item meta holder][BinaryItemMeta] for the ItemStack.
 */
interface ItemMetaHolder {

    /**
     * Gets a **snapshot** set which, at the time you called this function,
     * contains all the existing [BinaryItemMeta] on the item, which means that it is safe
     * to call [BinaryItemMeta.get] on every element in the set without throwing an exception
     * (except that you have called [BinaryItemMeta.remove] before [BinaryItemMeta.get]).
     */
    val snapshot: Set<BinaryItemMeta<*>>

    /**
     * Gets the holder of specific binary item meta.
     *
     * @param M the binary item meta type
     * @param clazz the binary item meta clazz
     * @return the binary item meta instance
     */
    fun <M : BinaryItemMeta<*>> get(clazz: KClass<out M>): M?

    /**
     * Gets the holder of specific binary item meta or create it, if it does not exist.
     *
     * @param M the binary item meta type
     * @param clazz the binary item meta clazz
     * @return the binary item meta instance
     */
    fun <M : BinaryItemMeta<*>> getOrCreate(clazz: KClass<out M>): M

}

/**
 * @see ItemMetaHolder.get
 */
inline fun <reified M : BinaryItemMeta<*>> ItemMetaHolder.get(): M? {
    return this.get(M::class)
}

/**
 * @see ItemMetaHolder.getOrCreate
 */
inline fun <reified M : BinaryItemMeta<*>> ItemMetaHolder.getOrCreate(): M {
    return this.getOrCreate(M::class)
}
package cc.mewcraft.wakame.item.binary.meta

import kotlin.reflect.KClass

/**
 * This is an interface to get specific [item meta accessor][BinaryItemMeta] for the ItemStack.
 */
interface ItemMetaAccessor {

    /**
     * Gets an immutable set of **snapshot** which, at the time you called this function,
     * contains all the **existing** [BinaryItemMeta] on the item, which means that it is safe
     * to call [BinaryItemMeta.get] on every element in the set without throwing an exception
     * (unless you have called [BinaryItemMeta.remove] before [BinaryItemMeta.get]).
     */
    val snapshot: Set<BinaryItemMeta<*>>

    /**
     * Gets the data accessor of specific binary item meta.
     *
     * The data accessor is always available even if the item meta does not exist in the ItemStack.
     *
     * @param M the binary item meta type
     * @param clazz the binary item meta clazz
     * @return the binary item meta instance
     */
    fun <M : BinaryItemMeta<*>> getAccessor(clazz: KClass<out M>): M

}

/**
 * @see ItemMetaAccessor.getAccessor
 */
inline fun <reified M : BinaryItemMeta<*>> ItemMetaAccessor.getAccessor(): M {
    return this.getAccessor(M::class)
}

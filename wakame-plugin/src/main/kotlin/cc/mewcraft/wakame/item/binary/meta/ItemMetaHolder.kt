package cc.mewcraft.wakame.item.binary.meta

import kotlin.reflect.KClass

interface ItemMetaHolder {

    /**
     * The map is a **snapshot** which, at the time you called this function,
     * contains all the existing [BinaryItemMeta] on the item, which means that it is safe
     * to call [BinaryItemMeta.get] on the entry values ([BinaryItemMeta]) without throwing
     * (except that you have called [BinaryItemMeta.remove] before [BinaryItemMeta.get]).
     */
    val map: Map<KClass<out BinaryItemMeta<*>>, BinaryItemMeta<*>>

    fun <T : BinaryItemMeta<V>, V> get(clazz: KClass<out T>): V?
    fun <T : BinaryItemMeta<V>, V> set(clazz: KClass<out T>, value: V)
    fun <T : BinaryItemMeta<*>> remove(clazz: KClass<out T>)

}

inline fun <reified T : BinaryItemMeta<V>, V> ItemMetaHolder.get(): V? {
    return this.get(T::class)
}

inline fun <reified T : BinaryItemMeta<V>, V> ItemMetaHolder.set(value: V) {
    this.set(T::class, value)
}

inline fun <reified T : BinaryItemMeta<*>> ItemMetaHolder.remove() {
    this.remove(T::class)
}
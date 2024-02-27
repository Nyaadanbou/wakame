package cc.mewcraft.wakame.item.binary.meta

import kotlin.reflect.KClass

interface ItemMetaHolder {

    /**
     * The map is a **snapshot** which, at the time you called this function,
     * contains all the existing [ItemMeta] on the item, which means that it is safe
     * to call [ItemMeta.get] on the entry values ([ItemMeta]) without throwing
     * (except that you have called [ItemMeta.remove] before [ItemMeta.get]).
     */
    val map: Map<KClass<out ItemMeta<*>>, ItemMeta<*>>

    fun <T : ItemMeta<*>> get(clazz: KClass<out T>): T?
    fun <T : ItemMeta<V>, V> set(clazz: KClass<out T>, value: V)
    fun <T : ItemMeta<*>> remove(clazz: KClass<out T>)

}

inline fun <reified T : ItemMeta<*>> ItemMetaHolder.get(): T? {
    return this.get(T::class)
}

inline fun <reified T : ItemMeta<V>, V> ItemMetaHolder.set(value: V) {
    this.set(T::class, value)
}

inline fun <reified T : ItemMeta<*>> ItemMetaHolder.remove() {
    this.remove(T::class)
}
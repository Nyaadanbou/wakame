package cc.mewcraft.wakame.item.binary.cell.reforge

import cc.mewcraft.wakame.item.ShadowTagLike
import kotlin.reflect.KClass

/**
 * Contains data used by the reforge system.
 *
 * @see ReforgeDataFactory
 */
interface ReforgeDataHolder : ShadowTagLike {
    /**
     * Check if the reforge data is empty.
     */
    val isEmpty: Boolean

    /**
     * Gets the data accessor for the specific reforge data.
     *
     * @param T the type of the reforge data
     * @param clazz the type of the reforge data
     * @return an data accessor
     */
    fun <T : ReforgeDataAccess<*>> access(clazz: KClass<T>): T
}

/**
 * @see ReforgeDataHolder.access
 */
inline fun <reified T : ReforgeDataAccess<*>> ReforgeDataHolder.access(): T {
    return this.access(T::class)
}

/**
 * A data accessor for the reforge data.
 *
 * @param T the type of the reforge data
 */
interface ReforgeDataAccess<T> {
    val exists: Boolean
    fun get(): T
    fun set(value: T)
    fun init()
}

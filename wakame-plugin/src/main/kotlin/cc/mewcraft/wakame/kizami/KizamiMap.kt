package cc.mewcraft.wakame.kizami

import kotlin.collections.Map

/**
 * Represents a map (kizami -> amount) owned by a subject.
 *
 * This object solely holds the amount of each kizami which is currently owned by the user,
 * and does not involve any operations of the effects provided by kizami. The operations of
 * kizami effects, such as "apply" and "remove", are defined by [KizamiEffect].
 */
interface KizamiMap : KizamiMapView, MutableIterable<Map.Entry<Kizami, Int>> {

    /**
     * Get a snapshot of the current kizami map.
     */
    fun getSnapshot(): KizamiMapSnapshot

    /**
     * Increment the amount of each kizami in the [kizami] by one.
     *
     * @param kizami the collection of kizami whose amount are to be incremented
     */
    fun addOneEach(kizami: Iterable<Kizami>)

    /**
     * Increment the amount of the specific [kizami] by one.
     *
     * @param kizami the kizami whose amount is to be incremented
     */
    fun addOne(kizami: Kizami)

    /**
     * Increment the amount of the specific [kizami] by specific [amount].
     *
     * @param kizami the kizami whose amount is to be incremented
     * @param amount the amount to be incremented by
     */
    fun add(kizami: Kizami, amount: Int)

    /**
     * Subtract the amount of each kizami in the [kizami] by one.
     *
     * @param kizami the collection of kizami whose amount are to be subtracted
     */
    fun subtractOneEach(kizami: Iterable<Kizami>)

    /**
     * Subtract the amount of the specific [kizami] by one.
     *
     * @param kizami the kizami whose amount is to be subtracted
     */
    fun subtractOne(kizami: Kizami)

    /**
     * Subtract the amount of the specific [kizami] by specific [amount].
     *
     * @param kizami the kizami whose amount is to be subtracted
     * @param amount the amount to be incremented by
     */
    fun subtract(kizami: Kizami, amount: Int)
}

/**
 * Represents a view of the kizami map. This interface is used to provide a read-only view of the kizami map.
 */
interface KizamiMapView : Iterable<Map.Entry<Kizami, Int>> {
    /**
     * Returns `true` if this map contains no entries.
     */
    fun isEmpty(): Boolean

    /**
     * Get the amount of specific kizami the player owns.
     *
     * The return value is always greater or equal to zero.
     */
    fun getAmount(kizami: Kizami): Int
}

/**
 * Represents a snapshot of the kizami map. No methods on this interface mutates the map.
 */
interface KizamiMapSnapshot : KizamiMapView

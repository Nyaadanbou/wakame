package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.user.User
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap

/**
 * Creates a new [KizamiMap].
 */
fun KizamiMap(user: User<*>): KizamiMap {
    return KizamiMapImpl(user)
}

/**
 * Represents a map (kizami -> amount) owned by a subject.
 *
 * This object solely holds the amount of each kizami which is currently owned by the user,
 * and does not involve any operations of the effects provided by kizami. The operations of
 * kizami effects, such as "apply" and "remove", are defined by [KizamiEffect].
 */
interface KizamiMap : KizamiMapView, MutableIterable<Map.Entry<KizamiType, Int>> {

    /**
     * Get a snapshot of the current kizami map.
     */
    fun getSnapshot(): KizamiMapSnapshot

    /**
     * Increment the amount of each kizami in the [kizami] by one.
     *
     * @param kizami the collection of kizami whose amount are to be incremented
     */
    fun addOneEach(kizami: Iterable<KizamiType>)

    /**
     * Increment the amount of the specific [kizami] by one.
     *
     * @param kizami the kizami whose amount is to be incremented
     */
    fun addOne(kizami: KizamiType)

    /**
     * Increment the amount of the specific [kizami] by specific [amount].
     *
     * @param kizami the kizami whose amount is to be incremented
     * @param amount the amount to be incremented by
     */
    fun add(kizami: KizamiType, amount: Int)

    /**
     * Subtract the amount of each kizami in the [kizami] by one.
     *
     * @param kizami the collection of kizami whose amount are to be subtracted
     */
    fun subtractOneEach(kizami: Iterable<KizamiType>)

    /**
     * Subtract the amount of the specific [kizami] by one.
     *
     * @param kizami the kizami whose amount is to be subtracted
     */
    fun subtractOne(kizami: KizamiType)

    /**
     * Subtract the amount of the specific [kizami] by specific [amount].
     *
     * @param kizami the kizami whose amount is to be subtracted
     * @param amount the amount to be incremented by
     */
    fun subtract(kizami: KizamiType, amount: Int)

    /**
     * 移除玩家身上所有的铭刻效果.
     */
    fun removeAllEffects()

    /**
     * 将所有铭刻效果应用到玩家身上.
     */
    fun applyAllEffects()
}

/**
 * Represents a view of the kizami map. This interface is used to provide a read-only view of the kizami map.
 */
interface KizamiMapView : Iterable<Map.Entry<KizamiType, Int>> {
    /**
     * Returns `true` if this map contains no entries.
     */
    fun isEmpty(): Boolean

    /**
     * Get the amount of specific kizami the player owns.
     *
     * The return value is always greater or equal to zero.
     */
    fun getAmount(kizami: KizamiType): Int
}

/**
 * Represents a snapshot of the kizami map. No methods on this interface mutates the map.
 */
interface KizamiMapSnapshot : KizamiMapView


/* Implementations */


/**
 * Each player will be associated with an instance of [KizamiMap].
 *
 * This class records the number of kizami each owned by a player.
 */
private class KizamiMapImpl(
    private val user: User<*>,
) : KizamiMap {
    private val amountMap: Object2IntOpenHashMap<KizamiType> = Object2IntOpenHashMap<KizamiType>().apply { defaultReturnValue(0) }

    override fun isEmpty(): Boolean {
        return amountMap.isEmpty()
    }

    override fun getAmount(kizami: KizamiType): Int {
        return amountMap.getInt(kizami)
    }

    override fun getSnapshot(): KizamiMapSnapshot {
        return KizamiMapSnapshotImpl(amountMap.clone())
    }

    override fun addOneEach(kizami: Iterable<KizamiType>) {
        kizami.forEach(this::addOne)
    }

    override fun addOne(kizami: KizamiType) {
        amountMap.mergeInt(kizami, 1) { oldAmount, _ -> oldAmount + 1 }
    }

    override fun add(kizami: KizamiType, amount: Int) {
        amountMap.mergeInt(kizami, amount) { oldAmount: Int, givenAmount: Int -> oldAmount + givenAmount }
    }

    override fun subtractOneEach(kizami: Iterable<KizamiType>) {
        kizami.forEach(::subtractOne)
    }

    override fun subtractOne(kizami: KizamiType) {
        amountMap.mergeInt(kizami, 0) { oldAmount, _ -> (oldAmount - 1).coerceAtLeast(0) }
    }

    override fun subtract(kizami: KizamiType, amount: Int) {
        amountMap.mergeInt(kizami, 0) { oldAmount, givenAmount -> (oldAmount - givenAmount).coerceAtLeast(0) }
    }

    override fun removeAllEffects() {
        for ((kizami, amount) in this) {
            kizami.effectMap[amount]?.forEach { it.remove(user) }
        }
    }

    override fun applyAllEffects() {
        for ((kizami, amount) in this) {
            kizami.effectMap[amount]?.forEach { it.apply(user) }
        }
    }

    override fun iterator(): MutableIterator<Map.Entry<KizamiType, Int>> {
        return amountMap.object2IntEntrySet().fastIterator()
    }
}

private class KizamiMapSnapshotImpl(
    private val amountMap: Object2IntOpenHashMap<KizamiType>,
) : KizamiMapSnapshot {
    override fun isEmpty(): Boolean {
        return amountMap.isEmpty()
    }

    override fun getAmount(kizami: KizamiType): Int {
        return amountMap.getInt(kizami)
    }

    override fun iterator(): MutableIterator<Map.Entry<KizamiType, Int>> {
        return amountMap.object2IntEntrySet().fastIterator()
    }
}
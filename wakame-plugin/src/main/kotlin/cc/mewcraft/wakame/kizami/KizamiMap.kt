package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.user.User
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap

/**
 * Represents a KizamiMap owned by a subject.
 */
interface KizamiMap {
    /**
     * Gets a Map view, where the `map key` is kizami and the `map value` is corresponding amount.
     *
     * @see getAmount
     */
    fun mapView(): Map<Kizami, Int>

    /**
     * Get the amount of specific kizami the player owns.
     *
     * The return value is always greater or equal to zero.
     */
    fun getAmount(kizami: Kizami): Int

    /**
     * Updates the player states with `this` [KizamiMap].
     *
     * This function essentially applies kizami effects to the [user],
     * such as adding attribute modifiers and removing active skills.
     */
    fun update(user: User)

    /* These functions will be called by inventory listeners */

    //<editor-fold desc="Update functions for Kizami amount">
    fun addOneEach(kizami: Iterable<Kizami>)
    fun addOne(kizami: Kizami)
    fun add(kizami: Kizami, amount: Int)
    fun subtractOneEach(kizami: Iterable<Kizami>)
    fun subtractOne(kizami: Kizami)
    fun subtract(kizami: Kizami, amount: Int)
    //</editor-fold>
}

/**
 * Creates a new [PlayerKizamiMap].
 */
fun PlayerKizamiMap(user: User): PlayerKizamiMap {
    return PlayerKizamiMap()
}

/**
 * Each player will be associated with an instance of [KizamiMap].
 *
 * This class records the number of kizami each owned by a player.
 */
class PlayerKizamiMap : KizamiMap {
    private val amountMap: MutableMap<Kizami, Int> = Reference2IntOpenHashMap()

    override fun mapView(): Map<Kizami, Int> {
        return amountMap
    }

    override fun getAmount(kizami: Kizami): Int {
        return amountMap[kizami] ?: 0
    }

    override fun update(user: User) {

    }

    override fun addOneEach(kizami: Iterable<Kizami>) {
        kizami.forEach(this::addOne)
    }

    override fun addOne(kizami: Kizami) {
        amountMap.merge(kizami, 1) { oldAmount, _ -> oldAmount + 1 }
    }

    override fun add(kizami: Kizami, amount: Int) {
        amountMap.merge(kizami, amount) { oldAmount, givenAmount -> oldAmount + givenAmount }
    }

    override fun subtractOneEach(kizami: Iterable<Kizami>) {
        kizami.forEach(::subtractOne)
    }

    override fun subtractOne(kizami: Kizami) {
        amountMap.merge(kizami, 0) { oldAmount, _ -> (oldAmount - 1).coerceAtLeast(0) }
    }

    override fun subtract(kizami: Kizami, amount: Int) {
        amountMap.merge(kizami, 0) { oldAmount, givenAmount -> (oldAmount - givenAmount).coerceAtLeast(0) }
    }
}
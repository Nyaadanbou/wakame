package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.player.Player
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap

/**
 * Each player will be associated with an instance of [KizamiMap].
 *
 * This class records the number of kizami each owned by a player.
 */
class KizamiMap {
    /**
     * Keeps track of the number of each kizami the player owns.
     */
    private val amountMap: MutableMap<Kizami, Int> = Reference2IntOpenHashMap()

    /**
     * Gets a Map view, where the `map key` is kizami and the `map value` is corresponding amount.
     *
     * @see getAmount
     */
    fun mapView(): Map<Kizami, Int> {
        return amountMap
    }

    /**
     * Get the amount of specific kizami the player owns.
     *
     * The return value is always greater or equal to zero.
     */
    fun getAmount(kizami: Kizami): Int {
        return amountMap[kizami] ?: 0
    }

    /**
     * Updates the player states with `this` [KizamiMap].
     *
     * This function essentially applies kizami effects to the [player],
     * such as adding attribute modifiers and removing active skills.
     */
    fun update(player: Player) {

    }

    /* These functions will be called by inventory listeners */

    //<editor-fold desc="Update functions for Kizami amount">
    fun addOneEach(kizami: Iterable<Kizami>) {
        kizami.forEach(this::addOne)
    }

    fun addOne(kizami: Kizami) {
        amountMap.merge(kizami, 1) { oldAmount, _ -> oldAmount + 1 }
    }

    fun add(kizami: Kizami, amount: Int) {
        amountMap.merge(kizami, amount) { oldAmount, givenAmount -> oldAmount + givenAmount }
    }

    fun subtractOneEach(kizami: Iterable<Kizami>) {
        kizami.forEach(::subtractOne)
    }

    fun subtractOne(kizami: Kizami) {
        amountMap.merge(kizami, 0) { oldAmount, _ -> (oldAmount - 1).coerceAtLeast(0) }
    }

    fun subtract(kizami: Kizami, amount: Int) {
        amountMap.merge(kizami, 0) { oldAmount, givenAmount -> (oldAmount - givenAmount).coerceAtLeast(0) }
    }
    //</editor-fold>
}
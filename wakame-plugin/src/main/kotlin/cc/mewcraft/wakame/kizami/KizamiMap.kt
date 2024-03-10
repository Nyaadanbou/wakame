package cc.mewcraft.wakame.kizami

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap

/**
 * Each player will be associated with an instance of [KizamiMap].
 *
 * This class records the number of specific kizamis owned by a player.
 */
class KizamiMap {
    /**
     * Keeps track of the number of each kizami the player owns.
     */
    private val amountMap: MutableMap<Kizami, Int> = Reference2IntOpenHashMap()

    /**
     * Gets a Map view, where the `map key` is kizami and the `map value` is corresponding amount.
     */
    fun mapView(): Map<Kizami, Int> {
        return amountMap
    }

    fun addOne(kizami: Kizami) {
        amountMap.merge(kizami, 1) { oldAmount, _ -> oldAmount + 1 }
    }

    fun add(kizami: Kizami, amount: Int) {
        amountMap.merge(kizami, amount) { oldAmount, givenAmount -> oldAmount + givenAmount }
    }

    fun subtractOne(kizami: Kizami) {
        amountMap.merge(kizami, 0) { oldAmount, _ -> (oldAmount - 1).coerceAtLeast(0) }
    }

    fun subtract(kizami: Kizami, amount: Int) {
        amountMap.merge(kizami, 0) { oldAmount, givenAmount -> (oldAmount - givenAmount).coerceAtLeast(0) }
    }

    /**
     * Get the amount of specific kizami the player owns.
     *
     * The return value is always greater or equal to zero.
     */
    fun getAmount(kizami: Kizami): Int {
        return amountMap[kizami] ?: 0
    }
}
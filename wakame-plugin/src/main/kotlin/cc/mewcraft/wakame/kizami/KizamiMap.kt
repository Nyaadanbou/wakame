package cc.mewcraft.wakame.kizami

/**
 * Represents a KizamiMap owned by a subject.
 */
interface KizamiMap {
    /**
     * Gets the mutable amount map, where the `map key` is kizami
     * and the `map value` is corresponding amount.
     *
     * @return the mutable amount map
     * @see getAmount
     */
    fun getMutableAmountMap(): MutableMap<Kizami, Int>

    /**
     * Gets the copy of amount map, where the `map key` is kizami
     * and the `map value` is corresponding amount.
     *
     * @return the copy of amount map
     */
    fun getImmutableAmountMap(): Map<Kizami, Int>

    /**
     * Get the amount of specific kizami the player owns.
     *
     * The return value is always greater or equal to zero.
     */
    fun getAmount(kizami: Kizami): Int

    /* These functions should be called by inventory listeners */

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

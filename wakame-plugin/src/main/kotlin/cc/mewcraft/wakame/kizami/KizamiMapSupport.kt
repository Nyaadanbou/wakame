package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.user.User
import com.google.common.collect.ImmutableMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap

// /**
//  * Creates a new [PlayerKizamiMap].
//  */
// fun PlayerKizamiMap(user: User): PlayerKizamiMap {
//     return PlayerKizamiMap(user)
// }

/**
 * Each player will be associated with an instance of [KizamiMap].
 *
 * This class records the number of kizami each owned by a player.
 */
class PlayerKizamiMap(
    private val user: User<*>,
) : KizamiMap {
    private val amountMap: Object2IntOpenHashMap<Kizami> = Object2IntOpenHashMap<Kizami>().apply { defaultReturnValue(0) }

    override fun getMutableAmountMap(): MutableMap<Kizami, Int> {
        return amountMap
    }

    override fun getImmutableAmountMap(): Map<Kizami, Int> {
        return ImmutableMap.copyOf(amountMap)
    }

    override fun getAmount(kizami: Kizami): Int {
        return amountMap.getInt(kizami)
    }

    override fun addOneEach(kizami: Iterable<Kizami>) {
        kizami.forEach(this::addOne)
    }

    override fun addOne(kizami: Kizami) {
        amountMap.mergeInt(kizami, 1) { oldAmount, _ -> oldAmount + 1 }
    }

    override fun add(kizami: Kizami, amount: Int) {
        amountMap.mergeInt(kizami, amount) { oldAmount: Int, givenAmount: Int -> oldAmount + givenAmount }
    }

    override fun subtractOneEach(kizami: Iterable<Kizami>) {
        kizami.forEach(::subtractOne)
    }

    override fun subtractOne(kizami: Kizami) {
        amountMap.mergeInt(kizami, 0) { oldAmount, _ -> (oldAmount - 1).coerceAtLeast(0) }
    }

    override fun subtract(kizami: Kizami, amount: Int) {
        amountMap.mergeInt(kizami, 0) { oldAmount, givenAmount -> (oldAmount - givenAmount).coerceAtLeast(0) }
    }
}
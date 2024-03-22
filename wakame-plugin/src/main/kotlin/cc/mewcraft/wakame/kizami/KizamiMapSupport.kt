package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.user.User
import com.google.common.collect.ImmutableMap
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap

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
    private val user: User,
) : KizamiMap {
    private val amountMap: MutableMap<Kizami, Int> = Reference2IntOpenHashMap()

    override fun getMutableAmountMap(): MutableMap<Kizami, Int> {
        return amountMap
    }

    override fun getImmutableAmountMap(): Map<Kizami, Int> {
        return ImmutableMap.copyOf(amountMap)
    }

    override fun getAmount(kizami: Kizami): Int {
        return amountMap[kizami] ?: 0
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
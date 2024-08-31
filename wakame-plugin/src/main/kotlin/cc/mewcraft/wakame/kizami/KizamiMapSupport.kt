package cc.mewcraft.wakame.kizami

import cc.mewcraft.wakame.user.User
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap

/**
 * Creates a new [PlayerKizamiMap].
 */
fun PlayerKizamiMap(user: User<*>): PlayerKizamiMap {
    return PlayerKizamiMap()
}

/**
 * Each player will be associated with an instance of [KizamiMap].
 *
 * This class records the number of kizami each owned by a player.
 */
class PlayerKizamiMap : KizamiMap {
    private val amountMappings: Object2IntOpenHashMap<Kizami> = Object2IntOpenHashMap<Kizami>().apply { defaultReturnValue(0) }

    override fun isEmpty(): Boolean {
        return amountMappings.isEmpty()
    }

    override fun getAmount(kizami: Kizami): Int {
        return amountMappings.getInt(kizami)
    }

    override fun addOneEach(kizami: Iterable<Kizami>) {
        kizami.forEach(this::addOne)
    }

    override fun addOne(kizami: Kizami) {
        amountMappings.mergeInt(kizami, 1) { oldAmount, _ -> oldAmount + 1 }
    }

    override fun add(kizami: Kizami, amount: Int) {
        amountMappings.mergeInt(kizami, amount) { oldAmount: Int, givenAmount: Int -> oldAmount + givenAmount }
    }

    override fun subtractOneEach(kizami: Iterable<Kizami>) {
        kizami.forEach(::subtractOne)
    }

    override fun subtractOne(kizami: Kizami) {
        amountMappings.mergeInt(kizami, 0) { oldAmount, _ -> (oldAmount - 1).coerceAtLeast(0) }
    }

    override fun subtract(kizami: Kizami, amount: Int) {
        amountMappings.mergeInt(kizami, 0) { oldAmount, givenAmount -> (oldAmount - givenAmount).coerceAtLeast(0) }
    }

    override fun iterator(): MutableIterator<Map.Entry<Kizami, Int>> {
        return amountMappings.object2IntEntrySet().fastIterator()
    }
}
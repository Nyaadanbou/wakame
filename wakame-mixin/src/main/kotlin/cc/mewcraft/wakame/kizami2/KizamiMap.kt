package cc.mewcraft.wakame.kizami2

import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import org.bukkit.entity.Player


/**
 * Represents a map (kizami -> amount) owned by a subject.
 *
 * 该接口可以读取和修改玩家身上铭刻的数量 (修改数量的函数不涉及更新铭刻的效果):
 * - [addOneEach]
 * - [addOne]
 * - [add]
 * - [subtractOneEach]
 * - [subtractOne]
 * - [subtract]
 *
 * 如果要控制铭刻的效果, 如“应用”和“移除”铭刻的效果, 使用下列函数:
 * - [removeAllEffects]
 * - [applyAllEffects]
 */
interface KizamiMap : KizamiMapView, MutableIterable<Map.Entry<RegistryEntry<Kizami>, Int>>, Component<KizamiMap> {

    /**
     * Get a snapshot of the current kizami map.
     */
    fun getSnapshot(): KizamiMapSnapshot

    /**
     * Increment the amount of each kizami in the [kizami] by one.
     *
     * @param kizami the collection of kizami whose amount are to be incremented
     */
    fun addOneEach(kizami: Iterable<RegistryEntry<Kizami>>)

    /**
     * Increment the amount of the specific [kizami] by one.
     *
     * @param kizami the kizami whose amount is to be incremented
     */
    fun addOne(kizami: RegistryEntry<Kizami>)

    /**
     * Increment the amount of the specific [kizami] by specific [amount].
     *
     * @param kizami the kizami whose amount is to be incremented
     * @param amount the amount to be incremented by
     */
    fun add(kizami: RegistryEntry<Kizami>, amount: Int)

    /**
     * Subtract the amount of each kizami in the [kizami] by one.
     *
     * @param kizami the collection of kizami whose amount are to be subtracted
     */
    fun subtractOneEach(kizami: Iterable<RegistryEntry<Kizami>>)

    /**
     * Subtract the amount of the specific [kizami] by one.
     *
     * @param kizami the kizami whose amount is to be subtracted
     */
    fun subtractOne(kizami: RegistryEntry<Kizami>)

    /**
     * Subtract the amount of the specific [kizami] by specific [amount].
     *
     * @param kizami the kizami whose amount is to be subtracted
     * @param amount the amount to be incremented by
     */
    fun subtract(kizami: RegistryEntry<Kizami>, amount: Int)

    /**
     * 基于玩家 [player] 当前拥有的铭刻数量, 移除玩家身上所有的铭刻效果.
     */
    fun removeAllEffects(player: Player)

    /**
     * 基于玩家 [player] 当前拥有的铭刻数量, 将所有铭刻效果应用到玩家身上.
     */
    fun applyAllEffects(player: Player)

    // Fleks

    companion object : ComponentType<KizamiMap>() {

        /**
         * 创建一个新的 [KizamiMap].
         */
        fun create(): KizamiMap {
            return KizamiMapImpl()
        }

    }

    override fun type(): ComponentType<KizamiMap> = KizamiMap
}

/**
 * Represents a view of the kizami map. This interface is used to provide a read-only view of the kizami map.
 */
interface KizamiMapView : Iterable<Map.Entry<RegistryEntry<Kizami>, Int>> {
    /**
     * Returns `true` if this map contains no entries.
     */
    fun isEmpty(): Boolean

    /**
     * Get the amount of specific kizami the player owns.
     *
     * The return value is always greater or equal to zero.
     */
    fun getAmount(kizami: RegistryEntry<Kizami>): Int
}

/**
 * Represents a snapshot of the kizami map. No methods on this interface mutates the map.
 */
interface KizamiMapSnapshot : KizamiMapView


// ------------
// 内部实现
// ------------


/**
 * Each player will be associated with an instance of [KizamiMap].
 *
 * This class records the number of kizami each owned by a player.
 */
private class KizamiMapImpl : KizamiMap {
    private val amountMap = Object2IntOpenHashMap<RegistryEntry<Kizami>>().apply { defaultReturnValue(0) }

    override fun isEmpty(): Boolean {
        return amountMap.isEmpty()
    }

    override fun getAmount(kizami: RegistryEntry<Kizami>): Int {
        return amountMap.getInt(kizami)
    }

    override fun getSnapshot(): KizamiMapSnapshot {
        return KizamiMapSnapshotImpl(amountMap.clone())
    }

    override fun addOneEach(kizami: Iterable<RegistryEntry<Kizami>>) {
        kizami.forEach(this::addOne)
    }

    override fun addOne(kizami: RegistryEntry<Kizami>) {
        amountMap.mergeInt(kizami, 1) { oldAmount, _ -> oldAmount + 1 }
    }

    override fun add(kizami: RegistryEntry<Kizami>, amount: Int) {
        amountMap.mergeInt(kizami, amount) { oldAmount: Int, givenAmount: Int -> oldAmount + givenAmount }
    }

    override fun subtractOneEach(kizami: Iterable<RegistryEntry<Kizami>>) {
        kizami.forEach(::subtractOne)
    }

    override fun subtractOne(kizami: RegistryEntry<Kizami>) {
        amountMap.mergeInt(kizami, 0) { oldAmount, _ -> (oldAmount - 1).coerceAtLeast(0) }
    }

    override fun subtract(kizami: RegistryEntry<Kizami>, amount: Int) {
        amountMap.mergeInt(kizami, 0) { oldAmount, givenAmount -> (oldAmount - givenAmount).coerceAtLeast(0) }
    }

    override fun removeAllEffects(player: Player) {
        for ((kizami, amount) in this) {
            kizami.unwrap().effects[amount]?.forEach { it.remove(player) }
        }
    }

    override fun applyAllEffects(player: Player) {
        for ((kizami, amount) in this) {
            kizami.unwrap().effects[amount]?.forEach { it.apply(player) }
        }
    }

    override fun iterator(): MutableIterator<Map.Entry<RegistryEntry<Kizami>, Int>> {
        return amountMap.object2IntEntrySet().fastIterator()
    }
}

private class KizamiMapSnapshotImpl(
    private val amountMap: Object2IntOpenHashMap<RegistryEntry<Kizami>>,
) : KizamiMapSnapshot {
    override fun isEmpty(): Boolean {
        return amountMap.isEmpty()
    }

    override fun getAmount(kizami: RegistryEntry<Kizami>): Int {
        return amountMap.getInt(kizami)
    }

    override fun iterator(): Iterator<Map.Entry<RegistryEntry<Kizami>, Int>> {
        return amountMap.object2IntEntrySet().fastIterator()
    }
}
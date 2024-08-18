package cc.mewcraft.wakame.station.recipe

import cc.mewcraft.wakame.core.ItemX
import cc.mewcraft.wakame.util.removeItem
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

/**
 * [StationChoice] 的消耗器.
 * 储存某一特定的 [StationChoice] 将要应用到玩家上的消耗.
 * 同类的消耗器将使用同一个上下文(由 [ChoiceConsumerContextMap] 保证).
 */
interface ChoiceConsumer<T : ChoiceConsumerContext> {
    /**
     * 创建消耗器的初始上下文.
     */
    fun initializeContext(player: Player): T

    fun applyConsume(player: Player, contextMap: ChoiceConsumerContextMap)
}

/**
 * [StationChoice] 的消耗器使用的上下文.
 */
interface ChoiceConsumerContext {
    /**
     * 使用合成站的玩家.
     */
    val player: Player
}

/**
 * [ChoiceConsumer] 到对应上下文的映射.
 * 用于保证整个配方的消耗过程中同类的消耗器使用的是同一个上下文.
 */
class ChoiceConsumerContextMap(
    /**
     * 使用合成站的玩家.
     */
    val player: Player
) {
    private val data: MutableMap<ChoiceConsumer<*>, Any> = Reference2ObjectArrayMap()

    operator fun contains(key: ChoiceConsumer<*>): Boolean {
        return key in data
    }

    operator fun <T : ChoiceConsumerContext> get(key: ChoiceConsumer<T>): T {
        return data[key] as T
    }

    operator fun set(key: ChoiceConsumer<*>, value: Any) {
        data[key] = value
    }
}

/* Internals */


//<editor-fold desc="ChoiceConsumer">
internal object ItemChoiceConsumer : ChoiceConsumer<ItemChoiceConsumerContext>, KoinComponent {
    val logger: Logger by inject()

    override fun initializeContext(player: Player): ItemChoiceConsumerContext {
        return ItemChoiceConsumerContext(player)
    }

    override fun applyConsume(player: Player, contextMap: ChoiceConsumerContextMap) {
        val notRemoveItems = player.removeItem(contextMap[this].get())
        if (notRemoveItems.isNotEmpty()) throw RuntimeException(
            "Station was not correctly removing all items that the player needed to consume. This should not happen."
        )
    }
}

internal object ExpChoiceConsumer : ChoiceConsumer<ExpChoiceConsumerContext>, KoinComponent {
    val logger: Logger by inject()

    override fun initializeContext(player: Player): ExpChoiceConsumerContext {
        return ExpChoiceConsumerContext(player)
    }

    override fun applyConsume(player: Player, contextMap: ChoiceConsumerContextMap) {
        player.totalExperience -= contextMap[this].get()
    }
}
//</editor-fold>


//<editor-fold desc="ChoiceConsumerContext">
internal class ItemChoiceConsumerContext(
    override val player: Player
) : ChoiceConsumerContext {
    private val consumeItems: Object2IntOpenHashMap<ItemX> = Object2IntOpenHashMap()

    fun add(itemX: ItemX, amount: Int) {
        consumeItems.mergeInt(itemX, amount) { oldValue, newValue -> oldValue + newValue }
    }

    fun get(): Object2IntOpenHashMap<ItemX> {
        return consumeItems
    }
}

internal class ExpChoiceConsumerContext(
    override val player: Player,
) : ChoiceConsumerContext {
    private var consumeExp: Int = 0

    fun add(amount: Int) {
        consumeExp += amount
    }

    fun get(): Int {
        return consumeExp
    }
}
//</editor-fold>


package cc.mewcraft.wakame.craftingstation.recipe

import cc.mewcraft.wakame.item.ItemRef
import cc.mewcraft.wakame.util.removeItem
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
import org.bukkit.entity.Player

/**
 * [RecipeChoice] 的消费者.
 * 用于记录某一特定的 [RecipeChoice] 将要应用到玩家身上的消耗.
 * 同类消费者将使用同一个上下文, 由 [ChoiceConsumerContextMap] 保证.
 */
interface ChoiceConsumer<T : ChoiceConsumerContext> {
    /**
     * 创建消耗器的初始上下文.
     */
    fun initCtx(player: Player): T

    /**
     * 应用消耗.
     */
    fun consume(player: Player, contextMap: ChoiceConsumerContextMap)
}

/**
 * [RecipeChoice] 的消耗器使用的上下文.
 */
interface ChoiceConsumerContext {
    /**
     * 使用合成站的玩家.
     */
    val player: Player
}

/**
 * [ChoiceConsumer] -> [ChoiceConsumerContext] 的映射.
 * 用于保证整个配方的消耗过程中, 同类的消耗器使用的是同一个上下文.
 */
class ChoiceConsumerContextMap(
    /**
     * 使用合成站的玩家.
     */
    val player: Player,
) {
    private val data: MutableMap<ChoiceConsumer<*>, Any> = Reference2ObjectArrayMap()

    operator fun contains(key: ChoiceConsumer<*>): Boolean {
        return key in data
    }

    operator fun <T : ChoiceConsumerContext> get(key: ChoiceConsumer<T>): T {
        @Suppress("UNCHECKED_CAST")
        return data[key] as T
    }

    operator fun set(key: ChoiceConsumer<*>, value: Any) {
        data[key] = value
    }
}

/* Internals */


//<editor-fold desc="ChoiceConsumer">
internal object ItemChoiceConsumer : ChoiceConsumer<ItemChoiceConsumerContext> {
    override fun initCtx(player: Player): ItemChoiceConsumerContext {
        return ItemChoiceConsumerContext(player)
    }

    override fun consume(player: Player, contextMap: ChoiceConsumerContextMap) {
        val notRemoveItems = player.removeItem(contextMap[this].get())
        if (notRemoveItems.isNotEmpty()) {
            throw RuntimeException(
                "crafting station was not correctly removing all items that the player needed to consume. This is a bug!"
            )
        }
    }
}

internal object ExpChoiceConsumer : ChoiceConsumer<ExpChoiceConsumerContext> {
    override fun initCtx(player: Player): ExpChoiceConsumerContext {
        return ExpChoiceConsumerContext(player)
    }

    override fun consume(player: Player, contextMap: ChoiceConsumerContextMap) {
        player.totalExperience -= contextMap[this].get()
    }
}
//</editor-fold>


//<editor-fold desc="ChoiceConsumerContext">
internal class ItemChoiceConsumerContext(
    override val player: Player,
) : ChoiceConsumerContext {
    // 要被消耗掉的物品堆叠
    private val items: Object2IntOpenHashMap<ItemRef> = Object2IntOpenHashMap()

    fun add(itemRef: ItemRef, amount: Int) {
        items.mergeInt(itemRef, amount) { oldValue, newValue -> oldValue + newValue }
    }

    fun get(): Map<ItemRef, Int> {
        return items
    }
}

internal class ExpChoiceConsumerContext(
    override val player: Player,
) : ChoiceConsumerContext {
    // 要被消耗掉的经验值
    private var experience: Int = 0

    fun add(amount: Int) {
        experience += amount
    }

    fun get(): Int {
        return experience
    }
}
//</editor-fold>


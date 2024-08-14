package cc.mewcraft.wakame.station

import cc.mewcraft.wakame.core.ItemX
import cc.mewcraft.wakame.core.ItemXRegistry
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

/**
 * [StationChoice] 的检查器.
 * 检查特定的 [StationChoice] 在上下文的情景中是否被满足.
 * 同类的检查器将使用同一个上下文(由 [ChoiceCheckerContextMap] 保证).
 */
interface ChoiceChecker<T : ChoiceCheckerContext> {
    /**
     * 创建检查器的初始上下文.
     */
    fun initializeContext(player: Player): T
}

/**
 * [StationChoice] 的检查器使用的上下文.
 */
interface ChoiceCheckerContext {
    /**
     * 使用合成站的玩家.
     */
    val player: Player
}

/**
 * [ChoiceChecker] 到对应上下文的映射.
 * 用于保证整个配方的检查过程中同类的检查器使用的是同一个上下文.
 */
class ChoiceCheckerContextMap(
    /**
     * 使用合成站的玩家.
     */
    val player: Player
) {
    private val data: MutableMap<ChoiceChecker<*>, Any> = Reference2ObjectArrayMap()

    operator fun contains(key: ChoiceChecker<*>): Boolean {
        return key in data
    }

    operator fun <T : ChoiceCheckerContext> get(key: ChoiceChecker<T>): T {
        return data[key] as T
    }

    operator fun set(key: ChoiceChecker<*>, value: Any) {
        data[key] = value
    }
}


/* Internals */


//<editor-fold desc="ChoiceChecker">
internal object ItemChoiceChecker : ChoiceChecker<ItemChoiceCheckerContext>, KoinComponent {
    val logger: Logger by inject()

    override fun initializeContext(player: Player): ItemChoiceCheckerContext {
        return ItemChoiceCheckerContext(player)
    }
}

internal object ExpChoiceChecker : ChoiceChecker<ExpChoiceCheckerContext>, KoinComponent {
    val logger: Logger by inject()

    override fun initializeContext(player: Player): ExpChoiceCheckerContext {
        return ExpChoiceCheckerContext(player)
    }
}
//</editor-fold>


//<editor-fold desc="ChoiceCheckerContext">
internal class ItemChoiceCheckerContext(
    override val player: Player
) : ChoiceCheckerContext {
    val inventorySnapshot: Object2IntOpenHashMap<ItemX> = run {
        // 只搜索主背包(36格)的物品, 不搜索副手和盔甲
        val inventory = player.inventory
        val ret = Object2IntOpenHashMap<ItemX>(36)
        for (itemStack in inventory.storageContents) {
            if (itemStack == null) {
                continue
            }
            val itemX = ItemXRegistry.byItem(itemStack)
            val amount = itemStack.amount
            ret.mergeInt(itemX, amount) { old, given -> old + given }
        }
        ret
    }
}

internal class ExpChoiceCheckerContext(
    override val player: Player,
) : ChoiceCheckerContext {
    var experienceSnapshot: Int = player.totalExperience
}
//</editor-fold>

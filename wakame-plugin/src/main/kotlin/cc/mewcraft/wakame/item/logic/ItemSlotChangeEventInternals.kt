package cc.mewcraft.wakame.item.logic

import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.event.bukkit.PlayerItemSlotChangeEvent
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.ItemSlotRegistry
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.event
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack

/**
 * 该类属于事件 [PlayerItemSlotChangeEvent] 内部完整实现的一部分.
 *
 * 该类扫描玩家背包的变化, 按情况触发 [PlayerItemSlotChangeEvent].
 *
 * 具体来说:
 * 我们每 tick 扫描每个玩家背包里的特定槽位上的物品.
 * 如果第 `n` tick 扫描的结果和第 `n-1` tick 扫描的结果不同,
 * 则认为这个槽位发生了变化, 那么此时就会触发一个事件.
 */
@Init(
    stage = InitStage.POST_WORLD,
)
internal object ItemSlotChangeEventInternals {

    private val lastItemRecords: LoadingCache<Player, LastItemRecord> = Caffeine.newBuilder().build { LastItemRecord() }

    @InitFun
    fun init() {
        // 每 tick 开始时 扫描玩家背包的变化.
        //
        // 必须在 tick 开始时而非结束时扫描背包+应用效果,
        // 这样可以让那些依赖装备效果的逻辑能够正确执行,
        // 例如: 同步当前生命值.
        event<ServerTickStartEvent> {
            val everyItemSlot = ItemSlotRegistry.all()
            for (player in SERVER.onlinePlayers) {
                val user = player.toUser()
                if (!user.isInventoryListenable) {
                    // 当玩家的背包不可监听时, 跳过扫描, 跳过触发事件.
                    // 换句话说, 在 isInventoryListenable 为 false 时,
                    // lastItemRecords 永远不会更新, 并且 isEmpty 为 true.
                    // 直到当 isInventoryListenable 为 true 时,
                    // lastItemRecords 才会开始更新.
                    continue
                }

                for (itemSlot in everyItemSlot) {
                    val currItemStack = itemSlot.getItem(player)
                    val lastItemStack = lastItemRecords[player][itemSlot]
                    if (currItemStack != lastItemStack) {
                        lastItemRecords[player][itemSlot] = currItemStack
                        val changeEvent = PlayerItemSlotChangeEvent(player, itemSlot, lastItemStack, currItemStack)
                        changeEvent.callEvent()
                    }
                }
            }
        }

        // 玩家下线时, 移除其记录.
        event<PlayerQuitEvent> {
            lastItemRecords.invalidate(it.player)
        }
    }
}

private class LastItemRecord {
    /**
     * 保存了每个槽位在 `t-1` 刻的物品.
     *
     * ## 注意!
     *
     * 该映射不储存 [ItemStack.isEmpty] 为 `true` 的物品. 对于这类物品, 该映射会直接将其储存为 `null`.
     */
    private val lastItems: Reference2ObjectOpenHashMap<ItemSlot, ItemStack?> = Reference2ObjectOpenHashMap(ItemSlotRegistry.size + 1, 0.99f)

    /**
     * 获取上一个 tick 的物品.
     *
     * 该函数不会返回 [ItemStack.isEmpty] 为 `true` 的物品! 对于这类物品, 该函数会直接返回 `null` 来表示它们.
     */
    operator fun get(itemSlot: ItemSlot): ItemStack? {
        return lastItems[itemSlot]
    }

    /**
     * 储存当前 tick 的物品.
     *
     * 你必须保证传入该函数的 [itemStack] 的 [ItemStack.isEmpty] 不返回 `true`. 对于这类物品, 你应该直接传入 `null`.
     */
    operator fun set(itemSlot: ItemSlot, itemStack: ItemStack?) {
        lastItems.put(itemSlot, itemStack?.clone())
    }
}

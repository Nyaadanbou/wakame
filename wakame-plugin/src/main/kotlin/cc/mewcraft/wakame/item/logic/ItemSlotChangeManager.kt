package cc.mewcraft.wakame.item.logic

import cc.mewcraft.wakame.event.PlayerItemSlotChangeEvent
import cc.mewcraft.wakame.item.DefaultItemSlotRegistry
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.user.toUser
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * 负责扫描玩家背包的变化, 根据情况触发 [PlayerItemSlotChangeEvent].
 *
 * 我们每 tick 扫描每个玩家背包里的特定槽位上的物品.
 * 如果第 `n` tick 扫描的结果和第 `n-1` tick 扫描的结果不同,
 * 则认为这个槽位发生了变化, 那么此时就会触发一个事件.
 */
internal class ItemSlotChangeManager : Listener, KoinComponent {
    private val server: Server = get()
    private val lastItemRecords: LoadingCache<Player, LastItemRecord> = Caffeine
        .newBuilder()
        .weakKeys()
        .build { LastItemRecord() }

    /**
     * 每 tick **开始时** 扫描玩家背包的变化.
     */
    // 开发日记 2024/11/17
    // 必须在 tick 开始时而非结束时扫描背包+应用效果,
    // 这样可以让那些依赖装备效果的逻辑能够正确执行,
    // 例如: 同步当前生命值.
    @EventHandler
    fun on(e: ServerTickStartEvent) {
        val everyItemSlot = DefaultItemSlotRegistry.all()
        for (player in server.onlinePlayers) {
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

    @EventHandler
    fun on(e: PlayerQuitEvent) {
        lastItemRecords.invalidate(e.player)
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
    private val lastItems: Reference2ObjectOpenHashMap<ItemSlot, ItemStack?> = Reference2ObjectOpenHashMap(DefaultItemSlotRegistry.size)

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

package cc.mewcraft.wakame.world.player.death

import cc.mewcraft.wakame.config.MAIN_CONFIG
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.bukkit.entity.EntityType
import org.bukkit.event.*
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import java.util.UUID

/**
 * 保护由玩家死亡掉落产生的物品实体.
 *
 * 目前的功能:
 * - 仅允许物品的所有者拾取掉落的物品
 *
 * 未来的功能:
 */
class PlayerDeathProtect : Listener {

    // 记录了将要掉落的物品堆叠, 以及物品对应的所有者
    private val deathDropRecords: Cache<ItemStack, UUID> = Caffeine.newBuilder().build()

    private val onlyOwnerCanPickupDeathDrops: Boolean by MAIN_CONFIG.entry("only_owner_can_pickup_death_drops")

    @EventHandler(
        ignoreCancelled = true,
        priority = EventPriority.MONITOR
    )
    fun on(event: PlayerDeathEvent) {
        if (!onlyOwnerCanPickupDeathDrops) {
            return
        }

        if (event.keepInventory) {
            return
        }

        val player = event.player
        val drops = event.drops
        for (drop in drops) {
            deathDropRecords.put(drop, player.uniqueId)
        }
    }

    @EventHandler(
        ignoreCancelled = true,
        // 设置 Item#owner 应该不会造成什么问题, 所以优先级设置为高
        priority = EventPriority.HIGHEST
    )
    fun on(event: ItemSpawnEvent) {
        if (!onlyOwnerCanPickupDeathDrops) {
            return
        }

        val entityType = event.entityType
        if (entityType != EntityType.ITEM) {
            return
        }

        val item = event.entity
        val itemStack = item.itemStack
        val ownerUuid = deathDropRecords.getIfPresent(itemStack) ?: return

        // 设置物品的所有者
        item.owner = ownerUuid

        // 移除记录
        deathDropRecords.invalidate(itemStack)
    }
}
package cc.mewcraft.wakame.world.player.death

import cc.mewcraft.wakame.config.MAIN_CONFIG
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.RemovalCause
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.slf4j.Logger
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * 保护由玩家死亡掉落产生的物品实体.
 *
 * 目前的功能:
 * - 仅允许物品的所有者拾取掉落的物品
 *
 * 未来的功能:
 * - 兼容其他系统???
 */
class PlayerDeathProtect : Listener, KoinComponent {

    private val logger: Logger = get()

    // 记录了将要掉落的物品堆叠, 以及物品对应的所有者
    private val deathDropRecords: Cache<ItemStack, UUID> = Caffeine.newBuilder()
        // 为了防止任何潜在的内存泄漏, 设置个保底的过期时间.
        // 理论上一旦玩家死亡爆了物品, 那么缓存应该就立马会被读取, 并被无效化.
        // 理论上这个过期永远都不应该自动触发, 如果是那应该是 BUG.
        .expireAfterWrite(10, TimeUnit.SECONDS)
        .removalListener<ItemStack, UUID> { key, value, cause ->
            if (cause != RemovalCause.EXPLICIT) {
                logger.warn("[PlayerDeathProtect] ItemStack $key was evicted (but not explicitly) from cache. This is a bug!")
            }
        }
        .build()

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
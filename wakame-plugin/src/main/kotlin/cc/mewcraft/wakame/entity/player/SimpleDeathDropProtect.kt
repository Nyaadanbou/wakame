package cc.mewcraft.wakame.entity.player

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.registerEvents
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.cache.RemovalCause
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import java.util.*
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
@Init(stage = InitStage.POST_WORLD)
internal object SimpleDeathDropProtect : Listener {

    @InitFun
    fun init() {
        registerEvents()
    }

    // 记录了将要掉落的物品堆叠, 以及物品对应的所有者
    private val deathDropRecords: Cache<ItemStack, UUID> = CacheBuilder.newBuilder()
        // 为了防止任何潜在的内存泄漏, 设置个保底的过期时间.
        // 理论上一旦玩家死亡爆了物品, 那么缓存应该就立马会被读取, 并被无效化.
        // 理论上这个过期永远都不应该自动触发, 如果是那应该是 BUG.
        .expireAfterWrite(10, TimeUnit.SECONDS)
        .removalListener<ItemStack, UUID> { notification ->
            if (notification.cause != RemovalCause.EXPLICIT) {
                LOGGER.warn("[${this::class.simpleName}] ItemStack ${notification.key} was evicted (but not explicitly) from cache. This is a bug!")
            }
        }
        .build()

    private val onlyOwnerCanPickupDeathDrops: Boolean by MAIN_CONFIG.entry<Boolean>("only_owner_can_pickup_death_drops")

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerDeathEvent) {
        if (!onlyOwnerCanPickupDeathDrops) {
            return
        }

        val player = event.player
        if (event.keepInventory) {
            return
        }

        val drops = event.drops
        for (drop in drops) {
            deathDropRecords.put(drop, player.uniqueId)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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
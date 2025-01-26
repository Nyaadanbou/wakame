package cc.mewcraft.wakame.world.player.death

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.config.MAIN_CONFIG
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.util.event
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.RemovalCause
import org.bukkit.entity.EntityType
import org.bukkit.event.EventPriority
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
@Init(
    stage = InitStage.POST_WORLD,
)
internal object PlayerDeathProtect {

    @InitFun
    fun init() {
        registerListeners()
    }

    // 记录了将要掉落的物品堆叠, 以及物品对应的所有者
    private val deathDropRecords: Cache<ItemStack, UUID> = Caffeine.newBuilder()
        // 为了防止任何潜在的内存泄漏, 设置个保底的过期时间.
        // 理论上一旦玩家死亡爆了物品, 那么缓存应该就立马会被读取, 并被无效化.
        // 理论上这个过期永远都不应该自动触发, 如果是那应该是 BUG.
        .expireAfterWrite(10, TimeUnit.SECONDS)
        .removalListener<ItemStack, UUID> { key, _, cause ->
            if (cause != RemovalCause.EXPLICIT) {
                LOGGER.warn("[${this::class.simpleName}] ItemStack $key was evicted (but not explicitly) from cache. This is a bug!")
            }
        }
        .build()

    private val onlyOwnerCanPickupDeathDrops: Boolean by MAIN_CONFIG.entry<Boolean>("only_owner_can_pickup_death_drops")

    private fun registerListeners() {

        // 在玩家死亡的时候, 记录掉落的物品堆叠, 以及物品对应的所有者
        event<PlayerDeathEvent>(EventPriority.MONITOR, true) { event->
            if (!onlyOwnerCanPickupDeathDrops) {
                return@event
            }

            if (event.keepInventory) {
                return@event
            }

            val player = event.player
            val drops = event.drops
            for (drop in drops) {
                deathDropRecords.put(drop, player.uniqueId)
            }
        }

        // 在物品掉落时, 设置物品的所有者, 并移除记录
        event<ItemSpawnEvent>(EventPriority.HIGHEST, true) { event->
            if (!onlyOwnerCanPickupDeathDrops) {
                return@event
            }

            val entityType = event.entityType
            if (entityType != EntityType.ITEM) {
                return@event
            }

            val item = event.entity
            val itemStack = item.itemStack
            val ownerUuid = deathDropRecords.getIfPresent(itemStack) ?: return@event

            // 设置物品的所有者
            item.owner = ownerUuid

            // 移除记录
           deathDropRecords.invalidate(itemStack)
        }
    }

}
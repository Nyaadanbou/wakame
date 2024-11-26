package cc.mewcraft.wakame.hook.impl.mythicmobs.listener

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.RemovalCause
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent
import io.lumine.mythic.bukkit.events.MythicMobItemGenerateEvent
import io.lumine.mythic.bukkit.events.MythicMobLootDropEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.slf4j.Logger
import java.util.UUID
import java.util.concurrent.TimeUnit

// 临时修复 FancyDrops 产生的物品实体依然会被其他玩家拾取的问题

@Deprecated("临时修复 - 如果 MM 修复了那么这个就不需要了")
object MobDropListener : KoinComponent, Listener {
    private val logger: Logger = get()

    // 记录MM怪物将要掉落的物品堆叠, 以及物品对应的所有者
    private val mobDropRecords: Cache<ItemStack, UUID> = Caffeine.newBuilder()
        // 为了防止任何潜在的内存泄漏, 设置个保底的过期时间.
        // 理论上一旦玩家死亡爆了物品, 那么缓存应该就立马会被读取, 并被无效化.
        // 理论上这个过期永远都不应该自动触发, 如果是那应该是 BUG.
        .expireAfterWrite(10, TimeUnit.SECONDS)
        .removalListener<ItemStack, UUID> { key, value, cause ->
            if (cause != RemovalCause.EXPLICIT) {
                logger.warn("[MobDropListener] ItemStack $key was evicted (but not explicitly) from cache. This is a bug!")
            }
        }
        .build()

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(e: MythicMobLootDropEvent) {
        val killer = e.killer as? Player ?: return
        val drops = e.drops
        // foobar
    }

    @EventHandler
    fun on(e: MythicMobItemGenerateEvent) {
        val trigger = e.trigger as? Player ?: return
        val itemStack = e.itemStack
        // foobar
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun on(e: MythicMobDeathEvent) {
        val killer = e.killer as? Player ?: return
        val drops = e.drops
        for (stack in drops) {
            mobDropRecords.put(stack, killer.uniqueId)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun on(e: ItemSpawnEvent) {
        val item = e.entity.itemStack
        val owner = mobDropRecords.getIfPresent(item) ?: return
        e.entity.owner = owner
        mobDropRecords.invalidate(item)
    }
}
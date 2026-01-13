package cc.mewcraft.wakame.hook.impl.mythicmobs.listener

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.util.event
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.cache.RemovalCause
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent
import io.lumine.mythic.bukkit.events.MythicMobItemGenerateEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.TimeUnit

// 临时修复 FancyDrops 产生的物品实体依然会被其他玩家拾取的问题
// 如果 MM 修复了这个问题那么这个 object 就不需要了

//@Init(InitStage.POST_WORLD)
object MobDropListener {

    //@InitFun
    fun init() {
        registerListeners()
    }

    // 记录MM怪物将要掉落的物品堆叠, 以及物品对应的所有者
    private val mobDropRecords: Cache<ItemStack, UUID> = CacheBuilder.newBuilder()
        // 为了防止任何潜在的内存泄漏, 设置个保底的过期时间.
        // 理论上一旦玩家死亡爆了物品, 那么缓存应该就立马会被读取, 并被无效化.
        // 理论上这个过期永远都不应该自动触发, 如果是那应该是 BUG.
        .expireAfterWrite(10, TimeUnit.SECONDS)
        .removalListener<ItemStack, UUID> { notification ->
            val k = notification.key
            val c = notification.cause
            if (c != RemovalCause.EXPLICIT) {
                LOGGER.warn("[${this::class.simpleName}] ItemStack $k was evicted (but not explicitly) from cache. This is a bug!")
            }
        }.build()

    private fun registerListeners() {
        event<MythicMobDeathEvent>(EventPriority.MONITOR) { event ->
            val killer = event.killer as? Player ?: return@event
            val drops = event.drops
            // foobar
        }

        event<MythicMobItemGenerateEvent> { event ->
            val trigger = event.trigger as? Player ?: return@event
            val itemStack = event.itemStack
            // foobar
        }

        event<MythicMobDeathEvent>(EventPriority.MONITOR) { event ->
            val killer = event.killer as? Player ?: return@event
            val drops = event.drops
            for (stack in drops) {
                mobDropRecords.put(stack, killer.uniqueId)
            }
        }

        event<ItemSpawnEvent>(EventPriority.HIGHEST) { event ->
            val item = event.entity.itemStack
            val owner = mobDropRecords.getIfPresent(item) ?: return@event
            event.entity.owner = owner
            mobDropRecords.invalidate(item)
        }
    }

}
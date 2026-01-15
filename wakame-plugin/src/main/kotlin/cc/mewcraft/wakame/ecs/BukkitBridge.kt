package cc.mewcraft.wakame.ecs

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ecs.bridge.koishify
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.registerEvents
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * 负责管理 [org.bukkit.entity.Player] 对应的 [cc.mewcraft.wakame.ecs.bridge.EEntity] 的生命周期
 */
@Init(InitStage.POST_WORLD)
object BukkitBridge : Listener {

    @InitFun
    fun init() {
        registerEvents()
    }

    // 在玩家进入服务器时, 为他创建对应的 ECS Entity
    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.koishify()
    }

    // 在玩家退出服务器时, 移除他对应的 ECS Entity
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        val koishify = player.koishify().apply { remove() }
        LOGGER.info("[ECS] $koishify removed for ${player.name} (${player.uniqueId})")
    }

}
package cc.mewcraft.wakame.entity.player

import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.registerEvents
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent


/**
 * 按比例缩放玩家的最大生命值.
 */
@Init(stage = InitStage.POST_WORLD)
internal object PlayerMaxHealthScale : Listener {

    @InitFun
    fun init() {
        registerEvents()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun on(event: PlayerJoinEvent) {
        val player = event.player
        // Koish 系统下玩家的最大生命值可以超过 20f,
        // 设置 healthScale 为 20f 避免红星占用过多屏幕
        // 但这也要求需要在其他地方显示玩家的当前/最大生命值
        player.healthScale = 20.0
    }
}
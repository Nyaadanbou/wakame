package cc.mewcraft.wakame.feature

import cc.mewcraft.lazyconfig.access.entryOrElse
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

// 强制将所有指令转换为小写
// - /SPAWN -> /spawn
// - /Spawn -> /spawn
// - /HELP SPAWN -> /help SPAWN
// - /Help SPawn -> /help SPawn

class ForceCommandLowercase : Listener {

    private val enabled by FEATURE_CONFIG.entryOrElse(false, "force_command_lowercase")

    @EventHandler
    fun on(event: PlayerCommandPreprocessEvent) {
        if (!enabled) return

        val message = event.message // e.g. "/Help SPawn"
        val spaceIndex = message.indexOf(' ')
        if (spaceIndex == -1) {
            // 没有参数, 整个消息就是指令, 例如 "/SPAWN"
            event.message = message.lowercase()
        } else {
            // 有参数, 只将指令部分转换为小写, 例如 "/HELP SPAWN" -> "/help SPAWN"
            val command = message.substring(0, spaceIndex).lowercase()
            val args = message.substring(spaceIndex)
            event.message = command + args
        }
    }
}

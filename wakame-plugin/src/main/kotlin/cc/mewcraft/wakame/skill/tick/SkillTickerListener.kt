package cc.mewcraft.wakame.skill.tick

import cc.mewcraft.wakame.user.toUser
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class SkillTickerListener(
    private val server: Server
) : Listener {

    @EventHandler
    private fun onTick(tick: ServerTickStartEvent) {
        for (player in server.onlinePlayers) {
            val user = player.toUser()
            user.skillState.tick()
        }
        SkillTicker.tick()
    }

}
package cc.mewcraft.wakame.skill.tick

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.tick.AlwaysTickable
import cc.mewcraft.wakame.tick.Ticker
import cc.mewcraft.wakame.user.toUser
import org.bukkit.Server
import java.util.UUID

class SkillTicker(
    private val server: Server
) : Initializable {
    private var taskId: UUID? = null

    fun start() {
        val alwaysTickable = AlwaysTickable {
            for (player in server.onlinePlayers) {
                player.toUser().skillState.tick()
            }
        }
        taskId = Ticker.addTick(alwaysTickable)
    }

    override fun close() {
        taskId?.let { Ticker.stopTick(it) }
    }

    override fun onPreWorld() {
        start()
    }

    override fun onReload() {
        close()
        start()
    }
}
package cc.mewcraft.wakame.skill.tick

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.tick.AlwaysTickable
import cc.mewcraft.wakame.tick.Ticker
import cc.mewcraft.wakame.user.toUser
import org.bukkit.Server

class SkillTicker(
    private val server: Server,
    private val ticker: Ticker,
) : Initializable {
    private var taskId: Int? = null

    fun start() {
        val alwaysTickable = AlwaysTickable {
            for (player in server.onlinePlayers) {
                player.toUser().skillState.tick()
            }
        }
        taskId = ticker.schedule(alwaysTickable)
    }

    override fun close() {
        taskId?.let { ticker.stopTick(it) }
    }

    override fun onPreWorld() {
        start()
    }

    override fun onReload() {
        close()
        start()
    }
}
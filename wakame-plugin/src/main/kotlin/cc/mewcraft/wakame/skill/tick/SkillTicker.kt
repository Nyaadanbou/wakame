package cc.mewcraft.wakame.skill.tick

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.tick.WakameTicker
import cc.mewcraft.wakame.user.toUser
import org.bukkit.Server
import java.util.UUID

class SkillTicker(
    private val server: Server
) : Initializable {
    private var taskId: UUID? = null

    fun start() {
        taskId = WakameTicker.addTickAlwaysExecuted { server.onlinePlayers.forEach { it.toUser().skillState.tick() } }
    }

    override fun close() {
        taskId?.let { WakameTicker.stopTick(it) }
    }

    override fun onPreWorld() {
        start()
    }

    override fun onReload() {
        close()
        start()
    }
}
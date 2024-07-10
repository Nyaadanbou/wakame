package cc.mewcraft.wakame.resource

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.tick.AlwaysTickable
import cc.mewcraft.wakame.tick.Ticker
import cc.mewcraft.wakame.user.toUser
import org.bukkit.Server
import java.util.*

class ResourceTicker(
    private val server: Server
) : Initializable {
    private var taskId: UUID? = null

    fun start() {
        val alwaysTickable = AlwaysTickable {
            server.onlinePlayers.forEach {
                val user = it.toUser()
                user.resourceMap.add(ResourceTypeRegistry.MANA, 1)
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
package cc.mewcraft.wakame.resource

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.tick.AlwaysTickable
import cc.mewcraft.wakame.tick.Ticker
import cc.mewcraft.wakame.user.toUser
import org.bukkit.Server

class ResourceTicker(
    private val server: Server
) : Initializable {
    private var taskId: Int? = null

    fun start() {
        val alwaysTickable = AlwaysTickable {
            server.onlinePlayers.forEach {
                val user = it.toUser()
                user.resourceMap.add(ResourceTypeRegistry.MANA, 1)
                user.player.level = user.resourceMap.current(ResourceTypeRegistry.MANA)
            }
        }
        taskId = Ticker.INSTANCE.addTick(alwaysTickable)
    }

    override fun close() {
        taskId?.let { Ticker.INSTANCE.stopTick(it) }
    }

    override fun onPreWorld() {
        start()
    }

    override fun onReload() {
        close()
        start()
    }
}
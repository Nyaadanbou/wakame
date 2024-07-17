package cc.mewcraft.wakame.resource

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.tick.AlwaysTickable
import cc.mewcraft.wakame.tick.Ticker
import cc.mewcraft.wakame.user.toUser
import org.bukkit.Server
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ResourceTicker(
    private val server: Server
) : Initializable, KoinComponent {
    private val ticker: Ticker by inject()

    private var taskId: Int? = null

    fun start() {
        val alwaysTickable = AlwaysTickable {
            server.onlinePlayers.forEach {
                val user = it.toUser()
                user.resourceMap.add(ResourceTypeRegistry.MANA, 1)
                user.player.level = user.resourceMap.current(ResourceTypeRegistry.MANA)
            }
        }
        taskId = ticker.addTick(alwaysTickable)
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
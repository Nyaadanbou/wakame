package cc.mewcraft.wakame.resource

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.runTaskTimer
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.scheduler.BukkitTask

class ResourceTicker(
    private val server: Server
) : Initializable {
    private var resourceTickTask: BukkitTask? = null

    fun start() {
        runTaskTimer(0, 1) {
            server.onlinePlayers.forEach { player ->
                val user = player.toUser()

                // 原型: 恢复魔法值
                user.resourceMap.add(ResourceTypeRegistry.MANA, 1)

                // 原型: 显示魔法值
                if (Bukkit.getServer().currentTick % 10 == 0) {
                    player.sendActionBar(Component.text("魔法值: ${user.resourceMap.current(ResourceTypeRegistry.MANA)}"))
                }
            }
        }.also {
            resourceTickTask = it
        }
    }

    override fun close() {
        resourceTickTask?.cancel()
    }

    override fun onPreWorld() {
        start()
    }

    override fun onReload() {
        close()
        start()
    }
}
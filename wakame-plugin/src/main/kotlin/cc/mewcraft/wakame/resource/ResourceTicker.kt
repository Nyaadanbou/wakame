package cc.mewcraft.wakame.resource

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.runTaskTimer
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.WeakHashMap

class ResourceTicker(
    private val server: Server,
) : Initializable {
    private val bossBarMap: WeakHashMap<Player, BossBar> = WeakHashMap()
    private var resourceTickTask: BukkitTask? = null

    fun start() {
        // 开始一个每 t 执行一次的循环任务,
        // 每次任务执行时:
        // * 恢复定量魔法值
        // * 显示当前魔法值

        runTaskTimer(
            delay = 0,
            period = 1
        ) {
            server.onlinePlayers.forEach { player ->
                val user = player.toUser()
                regenMana(user)
                showMana(user)
            }
        }.also {
            resourceTickTask = it
        }
    }

    private fun regenMana(user: User<Player>) {
        user.resourceMap.add(ResourceTypeRegistry.MANA, 1)
    }

    private fun showMana(user: User<Player>) {
        val player = user.player
        val current = user.resourceMap.current(ResourceTypeRegistry.MANA)
        val maximum = user.resourceMap.maximum(ResourceTypeRegistry.MANA)
        val progress = current.toFloat() / maximum.toFloat()
        val text = text { content("魔法值 $current / $maximum") }

        if (Bukkit.getServer().currentTick % 20 == 0) {
            val bossBar = bossBarMap.getOrPut(player) {
                BossBar.bossBar(Component.empty(), 0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS)
            }

            // 更新玩家的 bossBar
            bossBar.name(text)
            bossBar.progress(progress)

            // 展示给该玩家
            bossBar.addViewer(player)
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
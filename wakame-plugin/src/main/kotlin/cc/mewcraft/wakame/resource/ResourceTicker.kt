package cc.mewcraft.wakame.resource

import cc.mewcraft.wakame.initializer2.DisableFun
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

@Init(
    stage = InitStage.PRE_WORLD,
)
@Reload()
object ResourceTicker : KoinComponent {
    private val server: Server by inject()

    private val bossBarMap: WeakHashMap<Player, BossBar> = WeakHashMap()
    private var resourceTickTask: BukkitTask? = null

    fun start() {
        // 开始一个每 t 执行一次的循环任务,
        // 每次任务执行时:
        // * 恢复定量魔法值
        // * 显示当前魔法值

        // TODO 移除魔法值更新/显示, 等重构技能时再加回来
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

        if (Bukkit.getServer().currentTick % 5 == 0) {
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

    @DisableFun
    fun close() {
        resourceTickTask?.cancel()
    }

    @InitFun
    private fun onPreWorld() {
        start()
    }

    @ReloadFun
    private fun onReload() {
        close()
        start()
    }
}
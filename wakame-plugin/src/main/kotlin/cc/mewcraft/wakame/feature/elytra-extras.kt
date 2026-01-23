package cc.mewcraft.wakame.feature

import cc.mewcraft.lazyconfig.MAIN_CONFIG
import cc.mewcraft.lazyconfig.access.entryOrElse
import cc.mewcraft.wakame.integration.playermana.PlayerManaIntegration
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.registerEvents
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent
import com.destroystokyo.paper.event.server.ServerTickEndEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityToggleGlideEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.spongepowered.configurate.objectmapping.ConfigSerializable


@Init(InitStage.POST_WORLD)
object ElytraExtras {

    val config: ElytraExtrasConfig by MAIN_CONFIG.entryOrElse(ElytraExtrasConfig(), "elytra_extras")

    @InitFun
    fun init() {
        ElytraExtrasListener().registerEvents()
    }
}

@ConfigSerializable
data class ElytraExtrasConfig(
    val enabled: Boolean = false,
    val glideDrainPerSecond: Double = 1.0,
    val enterGlideManaCost: Double = 2.0,
    val rocketBoostManaCost: Double = 3.0,
)

/**
 * 玩家滑翔状态追踪数据类.
 *
 * @param ticksElapsed 已滑翔的tick数
 * @param lastDrainTick 上次进行每秒消耗的tick数
 */
private data class GlideState(
    var ticksElapsed: Long = 0L,
    var lastDrainTick: Long = 0L,
)

class ElytraExtrasListener : Listener {

    // 追踪玩家的滑翔状态: 玩家 -> 滑翔状态
    private val glideStateMap: HashMap<Player, GlideState> = HashMap()

    @EventHandler
    fun on(event: ServerTickEndEvent) {
        val config = ElytraExtras.config
        if (config.enabled.not()) return

        // 遍历所有正在滑翔的玩家
        val iterator = glideStateMap.iterator()
        while (iterator.hasNext()) {
            val (player, glideState) = iterator.next()

            // 检查玩家是否仍在滑翔
            if (!player.isGliding) {
                iterator.remove()
                continue
            }

            // 更新滑翔时长
            glideState.ticksElapsed++

            // 每 20 ticks (1 秒) 消耗一次魔法值
            if (glideState.ticksElapsed - glideState.lastDrainTick >= 20) {
                glideState.lastDrainTick = glideState.ticksElapsed

                if (!PlayerManaIntegration.consumeMana(player, config.glideDrainPerSecond)) {
                    // 魔法值不足，停止滑翔
                    player.isGliding = false
                    iterator.remove()
                }
            }
        }
    }

    @EventHandler
    fun on(event: EntityToggleGlideEvent) {
        val config = ElytraExtras.config
        if (config.enabled.not()) return

        val player = event.entity as? Player ?: return
        val enterGlideDrain = config.enterGlideManaCost

        if (event.isGliding) {
            // 进入滑翔状态:

            if (PlayerManaIntegration.getMana(player) < enterGlideDrain) {
                // 进入滑翔状态, 但是魔法值不足:

                event.isCancelled = true
                glideStateMap.remove(player)
            } else {
                // 进入滑翔状态, 并且魔法值足够:

                // 进入滑翔时消耗魔法值
                PlayerManaIntegration.consumeMana(player, enterGlideDrain)

                // 初始化滑翔状态
                glideStateMap[player] = GlideState()
            }
        } else {
            // 退出滑翔状态:

            glideStateMap.remove(player)
        }
    }

    @EventHandler
    fun on(event: PlayerElytraBoostEvent) {
        val config = ElytraExtras.config
        if (config.enabled.not()) return

        val player = event.player
        val rocketBoostDrain = config.rocketBoostManaCost
        if (!PlayerManaIntegration.consumeMana(player, rocketBoostDrain)) {
            // 使用烟花, 但是魔法值不足:

            event.isCancelled = true
        }
    }

    @EventHandler
    fun on(event: PlayerQuitEvent) {
        val config = ElytraExtras.config
        if (config.enabled.not()) return

        // 退出游戏时应该清理跟玩家相关的资源
        glideStateMap.remove(event.player)
    }
}
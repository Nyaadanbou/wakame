package cc.mewcraft.wakame.feature

import cc.mewcraft.wakame.integration.playermana.PlayerManaIntegration
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.item.property.impl.GlidingExtras
import cc.mewcraft.wakame.item.property.impl.ItemSlotRegistry
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.registerEvents
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent
import com.destroystokyo.paper.event.server.ServerTickEndEvent
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityToggleGlideEvent
import org.bukkit.event.player.PlayerQuitEvent


@Init(InitStage.POST_WORLD)
object ElytraExtras {

    @InitFun
    fun init() {
        ElytraExtrasListener().registerEvents()
    }
}

/**
 * 玩家滑翔状态追踪数据类.
 *
 * @param ticksElapsed 已滑翔的tick数
 * @param lastDrainTick 上次进行每秒消耗的tick数
 * @param glidingConfig 缓存的滑翔属性，避免重复查询
 */
private data class GlideState(
    var ticksElapsed: Long = 0L,
    var lastDrainTick: Long = 0L,
    val glidingConfig: GlidingExtras,
)

class ElytraExtrasListener : Listener {

    // 追踪玩家的滑翔状态: 玩家 -> 滑翔状态
    private val glideStateMap: Object2ObjectArrayMap<Player, GlideState> = Object2ObjectArrayMap()

    @EventHandler
    fun on(event: ServerTickEndEvent) {
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

                if (!PlayerManaIntegration.consumeMana(player, glideState.glidingConfig.glideDrainPerSecond)) {
                    // 魔法值不足，停止滑翔
                    player.isGliding = false
                    player.fallDistance = 0f
                    iterator.remove()
                }
            }
        }
    }

    @EventHandler
    fun on(event: EntityToggleGlideEvent) {
        val player = event.entity as? Player ?: return

        if (event.isGliding) {
            // 进入滑翔状态:

            val glidingExtras = getGlidingExtras(player) ?: return

            if (PlayerManaIntegration.consumeMana(player, glidingExtras.enterGlideManaCost)) {
                // 进入滑翔状态, 并且魔法值足够:

                // 初始化滑翔状态, 缓存 glidingExtras
                glideStateMap[player] = GlideState(glidingConfig = glidingExtras)
            } else {
                // 进入滑翔状态, 但是魔法值不足:

                event.isCancelled = true
                player.fallDistance = 0f
                glideStateMap.remove(player)
            }
        } else {
            // 退出滑翔状态:

            glideStateMap.remove(player)
        }
    }

    @EventHandler
    fun on(event: PlayerElytraBoostEvent) {
        val player = event.player
        val glidingExtras = glideStateMap[player]?.glidingConfig ?: return

        if (!PlayerManaIntegration.consumeMana(player, glidingExtras.rocketBoostManaCost)) {
            // 使用烟花, 但是魔法值不足:

            event.isCancelled = true
        }
    }

    @EventHandler
    fun on(event: PlayerQuitEvent) {
        // 退出游戏时应该清理跟玩家相关的资源
        glideStateMap.remove(event.player)
    }

    /**
     * 获取玩家当前的 [GlidingExtras].
     *
     * 实现细节: 遍历玩家的所有装备槽上的物品, 返回第一个找到的 [GlidingExtras].
     */
    private fun getGlidingExtras(player: Player): GlidingExtras? {
        return ItemSlotRegistry.itemSlots().firstNotNullOfOrNull { slot ->
            slot.getItem(player)?.getProp(ItemPropTypes.GLIDING_EXTRAS)
        }
    }
}
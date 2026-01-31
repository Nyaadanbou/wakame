package cc.mewcraft.wakame.feature

import cc.mewcraft.wakame.event.bukkit.PlayerItemSlotChangeEvent
import cc.mewcraft.wakame.integration.playermana.PlayerManaIntegration
import cc.mewcraft.wakame.item.ItemSlotChanges
import cc.mewcraft.wakame.item.getProp
import cc.mewcraft.wakame.item.hasProp
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.item.property.impl.GlidingExtras
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.metadata.metadata
import cc.mewcraft.wakame.util.metadata.metadataKey
import cc.mewcraft.wakame.util.registerEvents
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent
import com.destroystokyo.paper.event.server.ServerTickEndEvent
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityToggleGlideEvent
import org.bukkit.event.player.PlayerQuitEvent


// 当玩家滑翔时, 消耗其魔法值


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
 */
private data class GlideState(
    var ticksElapsed: Long = 0L,
    var lastDrainTick: Long = 0L,
)

class ElytraExtrasListener : Listener {

    companion object {
        val ACTIVATED_GLIDING_EXTRAS = metadataKey<GlidingExtras>("elytra_extras:activated_gliding_extras")
    }

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

                val glidingExtras = player.metadata().getOrNull(ACTIVATED_GLIDING_EXTRAS) ?: continue

                if (!PlayerManaIntegration.consumeMana(player, glidingExtras.glideDrainPerSecond)) {
                    // 魔法值不足，停止滑翔
                    player.isGliding = false
                    player.fallDistance = 0f
                    iterator.remove()
                }
            }
        }
    }

    @EventHandler
    fun on(event: PlayerItemSlotChangeEvent) {
        val player = event.player
        val slot = event.slot
        val prev = event.oldItemStack
        val curr = event.newItemStack
        if (prev != null &&
            ItemSlotChanges.testSlot(slot, prev)
        ) {
            if (prev.hasProp(ItemPropTypes.GLIDING_EXTRAS)) {
                player.metadata().remove(ACTIVATED_GLIDING_EXTRAS)
            }
        }

        if (curr != null &&
            ItemSlotChanges.testSlot(slot, curr) &&
            ItemSlotChanges.testLevel(player, curr) &&
            ItemSlotChanges.testDurability(curr)
        ) {
            val glidingExtras = curr.getProp(ItemPropTypes.GLIDING_EXTRAS)
            if (glidingExtras != null) {
                player.metadata().put(ACTIVATED_GLIDING_EXTRAS, glidingExtras)
            }
        }
    }

    @EventHandler
    fun on(event: EntityToggleGlideEvent) {
        val player = event.entity as? Player ?: return

        if (event.isGliding) {
            // 进入滑翔状态:

            val glidingExtras = player.metadata().getOrNull(ACTIVATED_GLIDING_EXTRAS) ?: return

            if (PlayerManaIntegration.consumeMana(player, glidingExtras.enterGlideManaCost)) {
                // 进入滑翔状态, 并且魔法值足够:

                // 初始化滑翔状态, 缓存 glidingExtras
                glideStateMap[player] = GlideState()
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
        val glidingExtras = player.metadata().getOrNull(ACTIVATED_GLIDING_EXTRAS) ?: return

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
}
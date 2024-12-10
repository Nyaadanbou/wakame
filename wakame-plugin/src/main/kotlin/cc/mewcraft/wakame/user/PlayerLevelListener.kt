package cc.mewcraft.wakame.user

import cc.mewcraft.adventurelevel.event.AdventureLevelDataLoadEvent
import cc.mewcraft.wakame.resource.ResourceSynchronizer
import cc.mewcraft.wakame.util.concurrent.isServerThread
import cc.mewcraft.wakame.util.runTask
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

internal object PlayerLevelListener : Listener {
    @EventHandler(
        // 尽可能早的储存 PDC, 让 HuskSync 能够同步到
        priority = EventPriority.LOWEST
    )
    fun on(e: PlayerQuitEvent) {
        val player = e.player

        // 保存玩家数据
        ResourceSynchronizer.save(player)
    }

    /**
     * ### 注意事项 2024/11/2
     *
     * 当服务器安装了 HuskSync 时, 这个 [cc.mewcraft.adventurelevel.event.AdventureLevelDataLoadEvent]
     * 会在 HuskSync 的 *同步完成* 事件之后触发. 也就是说, 如果 HuskSync
     * 没有完成同步, 那么该事件永远不会触发.
     */
    @EventHandler
    fun on(event: AdventureLevelDataLoadEvent) {
        val data = event.userData
        val player = Bukkit.getPlayer(data.uuid) ?: return
        val user = player.toUser()

        // 标记玩家的背包可以被监听了
        if (!user.isInventoryListenable) {
            user.isInventoryListenable = true
        }

        if (isServerThread) {
            // 如果事件是同步触发的, 那么直接操作玩家的状态
            syncResource(player)
        } else {
            // 如果事件是异步触发的, 必须在主线程操作玩家的状态
            runTask {
                syncResource(player)
            }
        }
    }

    /**
     * 修复玩家在加入服务器后, 最大生命值会被限制在 `max_health.base` 的相关问题.
     *
     * ### 解决方案
     *
     * 在玩家退出服务器时, 我们将他的当前最大生命值保存到 PDC 中.
     * 玩家加入服务器后, 我们将 PDC 中的最大生命值设置到玩家身上.
     *
     * 如果要考虑跨服, 那么我们将 *加入服务器时* 的时机换成 *数据完成同步时* 即可.
     *
     * ### 相关问题
     *
     * [MC-17876](https://bugs.mojang.com/browse/MC-17876).
     */
    private fun syncResource(player: Player) {
        ResourceSynchronizer.load(player)
    }
}
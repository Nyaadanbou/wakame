package cc.mewcraft.wakame.item.logic

import cc.mewcraft.adventurelevel.event.AdventureLevelDataLoadEvent
import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.resource.ResourceSynchronizer
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.runTaskLater
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.*
import org.bukkit.event.player.PlayerQuitEvent
import org.koin.core.component.inject

internal object AdventureLevelListener : Listener {
    private val resourceSynchronizer: ResourceSynchronizer by Injector.inject()

    @EventHandler(
        // 尽可能早的储存 PDC, 让 HuskSync 能够同步到
        priority = EventPriority.LOWEST
    )
    fun on(e: PlayerQuitEvent) {
        val player = e.player

        // 保存玩家数据
        resourceSynchronizer.save(player)
    }

    /**
     * ### 注意事项 2024/11/2
     *
     * 当服务器安装了 HuskSync 时, 这个 [AdventureLevelDataLoadEvent]
     * 会在 HuskSync 的 *同步完成* 事件之后触发. 也就是说, 如果 HuskSync
     * 没有完成同步, 那么该事件永远不会触发.
     */
    @EventHandler
    fun on(event: AdventureLevelDataLoadEvent) {
        val data = event.playerData
        val player = Bukkit.getPlayer(data.uuid) ?: return
        val user = player.toUser()

        // 标记玩家的背包可以被监听了
        user.isInventoryListenable = true

        // 当前事件是异步触发的, 必须在主线程操作玩家的状态
        runTaskLater(0) {
            syncResource(player)
        }
    }

    // /**
    //  * 修复与插件 AdventureLevel 异步加载数据的相关问题.
    //  *
    //  * ### 描述
    //  *
    //  * 玩家刚进服务器, 我们会根据玩家身上的物品来给予玩家特定效果.
    //  * 而物品能够给予效果的前期是, 玩家的冒险等级必须大于等于物品等级.
    //  * 如果冒险等级用的是原版经验等级, 那么这个问题就不存在, 因为数据加载是同步的.
    //  * 但是如果冒险等级用的是 AdventureLevel (数据库), 那么就有关于并发的问题了.
    //  * 这是因为 AdventureLevel 返回的冒险等级是一个类似 Deferred 的对象,
    //  * 这个对象一开始会返回 0 级; 只有当等级从数据库加载完毕时, 数值才会就绪.
    //  *
    //  * 结果就是, 玩家刚进游戏的一瞬间, 他的冒险等级其实是 0 级.
    //  * 这也就导致了, 玩家身上的对冒险等级有要求的物品无法给予效果.
    //  *
    //  * ### 解决办法
    //  *
    //  * 当数据加载完毕后, 我们强制更新玩家身上的物品, 使其给予玩家效果.
    //  */
    // private fun updateSlots(player: Player) {
    //     for (listener in ItemSlotChangeRegistry.listeners()) {
    //         listener.forceUpdate(player)
    //     }
    // }

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
        resourceSynchronizer.load(player)
    }
}

package cc.mewcraft.wakame.item.logic

import cc.mewcraft.adventurelevel.event.AdventureLevelDataLoadEvent
import cc.mewcraft.wakame.util.runTask
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

internal object AdventureLevelListener : Listener {

    // 开发日记 2024/10/7 小米
    // 虽然看起来是 hotfix, 但我想不出有什么更好的办法了.
    // 也许这种靠 callback 解决的方案, 本身就已经优雅了?
    /**
     * 修复与插件 AdventureLevel 异步加载数据的相关问题.
     *
     * ### 描述
     *
     * 玩家刚进服务器, 我们会根据玩家身上的物品来给予玩家特定效果.
     * 而物品能够给予效果的前期是, 玩家的冒险等级必须大于等于物品等级.
     * 如果冒险等级用的是原版经验等级, 那么这个问题就不存在, 因为数据加载是同步的.
     * 但是如果冒险等级用的是 AdventureLevel (数据库), 那么就有关于并发的问题了.
     * 这是因为 AdventureLevel 返回的冒险等级是一个类似 Deferred 的对象,
     * 这个对象一开始没有等级; 只有当等级从数据库加载完毕时, 数值才会就绪.
     *
     * 结果就是, 玩家刚进游戏的一瞬间, 他的冒险等级其实是 0 级.
     * 这也就导致了, 玩家身上的对冒险等级有要求的物品无法给予效果.
     *
     * ### 解决办法
     *
     * 当数据加载完毕后, 我们强制更新玩家身上的物品, 使其给予玩家效果.
     */
    @EventHandler
    fun on(event: AdventureLevelDataLoadEvent) {
        val data = event.playerData
        val player = Bukkit.getPlayer(data.uuid) ?: return

        // 当前事件是异步触发的, 而更新物品提供的效果
        // 需要访问世界状态, 所以强制安排到主线程执行
        runTask {
            for (listener in ItemSlotChangeRegistry.listeners()) {
                listener.forceUpdate(player)
            }
        }
    }
}

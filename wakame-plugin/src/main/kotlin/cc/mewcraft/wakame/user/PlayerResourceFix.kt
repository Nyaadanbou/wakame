package cc.mewcraft.wakame.user

import cc.mewcraft.adventurelevel.event.AdventureLevelDataLoadEvent
import cc.mewcraft.wakame.Koish
import cc.mewcraft.wakame.initializer2.DisableFun
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.integration.HooksLoader
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelManager
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelType
import cc.mewcraft.wakame.resource.ResourceSynchronizer
import cc.mewcraft.wakame.util.concurrent.isServerThread
import cc.mewcraft.wakame.util.event
import cc.mewcraft.wakame.util.runTask
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * 该单例用于解决玩家资源(当前血量,当前魔法值)在进出服务器时无法正确加载的问题.
 */
@Init(
    stage = InitStage.POST_WORLD,
    runAfter = [
        HooksLoader::class, // 依赖于 PlayerLevelManager
    ]
)
internal object PlayerResourceFix {

    @InitFun
    fun init() {

        // 在 PlayerQuitEvent 保存玩家的资源数据
        event<PlayerQuitEvent>(
            priority = EventPriority.LOWEST, // 尽可能早的储存 PDC, 让 HuskSync 能够同步到
        ) { event ->
            val player = event.player
            ResourceSynchronizer.save(player)
        }

        // 在冒险等级加载完毕后, 加载玩家的资源数据
        when (PlayerLevelManager.integration.type) {
            PlayerLevelType.ZERO,
            PlayerLevelType.VANILLA -> event<PlayerJoinEvent> { event ->
                val player = event.player
                val user = player.toUser()
                user.isInventoryListenable = true
                ResourceSynchronizer.load(player)
            }

            PlayerLevelType.ADVENTURE -> event<AdventureLevelDataLoadEvent> { event ->
                // 注意事项 2024/11/2:
                // 当服务器安装了 HuskSync 时, AdventureLevelDataLoadEvent 会在 HuskSync 的 BukkitSyncCompleteEvent 之后触发.
                // 也就是说, 如果 HuskSync 没有完成同步, 那么 AdventureLevelDataLoadEvent 永远不会触发.
                val data = event.userData
                val player = Bukkit.getPlayer(data.uuid) ?: return@event
                val user = player.toUser()

                // 标记玩家的背包可以被监听了
                if (!user.isInventoryListenable) {
                    user.isInventoryListenable = true
                }

                if (isServerThread) {
                    // 如果事件是同步触发的, 那么直接操作玩家的状态
                    ResourceSynchronizer.load(player)
                } else {
                    // 如果事件是异步触发的, 必须在主线程操作玩家的状态
                    runTask { ResourceSynchronizer.load(player) }
                }
            }
        }
    }

    @DisableFun
    fun close() {
        // 关闭服务器时服务端不会触发任何事件,
        // 需要我们手动执行保存玩家资源的逻辑.
        // 如果服务器有使用 HuskSync, 我们的插件必须在 HuskSync 之前关闭,
        // 否则 PDC 无法保存到 HuskSync 的数据库, 导致玩家资源数据丢失.
        ResourceSynchronizer.saveAll()
    }

}
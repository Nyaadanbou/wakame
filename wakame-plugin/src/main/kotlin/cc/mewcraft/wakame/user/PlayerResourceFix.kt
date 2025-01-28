package cc.mewcraft.wakame.user

import cc.mewcraft.adventurelevel.event.AdventureLevelDataLoadEvent
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.entity.resource.ResourceSynchronizer
import cc.mewcraft.wakame.integration.HooksLoader
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelManager
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelType
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.concurrent.isServerThread
import cc.mewcraft.wakame.util.event
import cc.mewcraft.wakame.util.runTask
import org.bukkit.Bukkit
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

        // 在冒险等级加载完毕后, 加载玩家的资源数据:
        // 这里根据运行时的冒险等级系统加载对应的监听器
        when (val type = PlayerLevelManager.integration.type) {

            // 这两冒险等级系统完全依赖原版游戏自身, 没有额外的数据储存,
            // 所以可以直接在进入游戏时读取玩家的等级信息并且加载资源数据.
            PlayerLevelType.ZERO,
            PlayerLevelType.VANILLA -> event<PlayerJoinEvent> { event ->
                val player = event.player
                val user = player.toUser()
                user.isInventoryListenable = true
                ResourceSynchronizer.load(player)
            }

            // 冒险等级插件的数据是异步加载, 需要处理一下线程同步问题.
            PlayerLevelType.ADVENTURE -> event<AdventureLevelDataLoadEvent> { event ->
                fun execute() {
                    // 注意事项 2024/11/2:
                    // 该逻辑高度依赖 HuskSync 的运行情况.
                    // 当服务器安装了 HuskSync 时, AdventureLevelDataLoadEvent 会在 HuskSync 的 BukkitSyncCompleteEvent 之后触发.
                    // 也就是说, 如果 HuskSync 没有完成同步, 那么 AdventureLevelDataLoadEvent 永远不会触发.
                    val data = event.userData
                    val player = Bukkit.getPlayer(data.uuid) ?: run {
                        LOGGER.warn("Player ${data.uuid} is not online, skipping resource synchronization")
                        return
                    }
                    val user = player.toUser()

                    // 标记玩家的背包可以被监听了
                    if (!user.isInventoryListenable) {
                        user.isInventoryListenable = true
                    }

                    ResourceSynchronizer.load(player)
                }

                if (isServerThread) {
                    execute()
                } else {
                    runTask(::execute)
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
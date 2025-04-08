package cc.mewcraft.wakame.hook.impl.adventurelevel

import cc.mewcraft.adventurelevel.event.AdventureLevelDataLoadEvent
import cc.mewcraft.adventurelevel.level.category.LevelCategory
import cc.mewcraft.adventurelevel.plugin.AdventureLevelProvider
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.entity.player.ResourceLoadingFixHandler
import cc.mewcraft.wakame.entity.player.ResourceSynchronizer
import cc.mewcraft.wakame.entity.player.isInventoryListenable
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelIntegration
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelType
import cc.mewcraft.wakame.util.event
import cc.mewcraft.wakame.util.runTask
import cc.mewcraft.wakame.util.runTaskLater
import java.util.*

/**
 * A [player level integration][PlayerLevelIntegration] that returns the
 * *adventure level* (i.e., the level from our AdventureLevel plugin).
 */
@Hook(plugins = ["AdventureLevel"])
object AdventureLevelHook : PlayerLevelIntegration, ResourceLoadingFixHandler {

    override val type: PlayerLevelType = PlayerLevelType.ADVENTURE

    override fun get(uuid: UUID): Int? {
        val api = AdventureLevelProvider.get()
        val userDataRepository = api.userDataRepository
        val userData = userDataRepository.getCached(uuid) ?: return null
        val primaryLevel = userData.getLevel(LevelCategory.PRIMARY)
        return primaryLevel.level
    }

    override fun invoke() {

        // 冒险等级插件的数据是异步加载, 需要处理一下线程同步问题.
        event<AdventureLevelDataLoadEvent> { event ->
            // 注意事项 2024/11/2:
            // 该逻辑高度依赖 HuskSync 的运行情况.
            // 当服务器安装了 HuskSync 时, AdventureLevelDataLoadEvent 会在 HuskSync 的 BukkitSyncCompleteEvent 之后触发.
            // 也就是说, 如果 HuskSync 没有完成同步, 那么 AdventureLevelDataLoadEvent 永远不会触发.
            val data = event.userData
            val player = SERVER.getPlayer(data.uuid) ?: run {
                LOGGER.warn("Player ${data.uuid} is not online, skipping resource synchronization")
                return@event
            }

            runTask {
                // 标记玩家的背包可以被监听了
                if (player.isConnected) {
                    player.isInventoryListenable = true
                }

                // TODO 除了延迟 1t 外还有更好维护的解决方式吗?
                runTaskLater(1) {
                    if (player.isConnected) {
                        ResourceSynchronizer.load(player)
                    }
                }
            }
        }

    }

}
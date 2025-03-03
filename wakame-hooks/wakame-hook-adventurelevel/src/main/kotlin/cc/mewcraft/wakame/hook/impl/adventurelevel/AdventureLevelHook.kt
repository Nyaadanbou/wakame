package cc.mewcraft.wakame.hook.impl.adventurelevel

import cc.mewcraft.adventurelevel.event.AdventureLevelDataLoadEvent
import cc.mewcraft.adventurelevel.level.category.LevelCategory
import cc.mewcraft.adventurelevel.plugin.AdventureLevelProvider
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.entity.resource.ResourceSynchronizer
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelIntegration
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelType
import cc.mewcraft.wakame.user.PlayerResourceFixExternalHandler
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.concurrent.isServerThread
import cc.mewcraft.wakame.util.event
import cc.mewcraft.wakame.util.runTask
import org.bukkit.Bukkit
import java.util.*

/**
 * A [player level integration][PlayerLevelIntegration] that returns the
 * *adventure level* (i.e., the level from our AdventureLevel plugin).
 */
@Hook(plugins = ["AdventureLevel"])
object AdventureLevelHook : PlayerLevelIntegration, PlayerResourceFixExternalHandler {

    override val type: PlayerLevelType = PlayerLevelType.ADVENTURE

    override fun get(uuid: UUID): Int? {
        val api = AdventureLevelProvider.get()
        val userDataRepository = api.userDataRepository
        val userData = userDataRepository.getCached(uuid) ?: return null
        val primaryLevel = userData.getLevel(LevelCategory.PRIMARY)
        return primaryLevel.level
    }

    override fun run() {

        // 冒险等级插件的数据是异步加载, 需要处理一下线程同步问题.
        event<AdventureLevelDataLoadEvent> { event ->
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
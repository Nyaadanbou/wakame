package cc.mewcraft.wakame.hook.impl.adventurelevel

import cc.mewcraft.adventurelevel.event.AdventureLevelDataLoadEvent
import cc.mewcraft.adventurelevel.level.category.LevelCategory
import cc.mewcraft.adventurelevel.plugin.AdventureLevelProvider
import cc.mewcraft.lazyconfig.MAIN_CONFIG
import cc.mewcraft.lazyconfig.access.entry
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.entity.player.PlayerDataLoadingCoordinator
import cc.mewcraft.wakame.entity.player.ResourceLoadingFixHandler
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelIntegration
import cc.mewcraft.wakame.integration.playerlevel.PlayerLevelType
import cc.mewcraft.wakame.util.event
import org.bukkit.Bukkit
import java.util.*

/**
 * A [player level integration][PlayerLevelIntegration] that returns the
 * *adventure level* (i.e., the level from our AdventureLevel plugin).
 */
@Hook(plugins = ["AdventureLevel"])
object AdventureLevelHook {

    private val PLAYER_LEVEL_PROVIDER by MAIN_CONFIG.entry<PlayerLevelType>("player_level_provider")

    init {
        if (PLAYER_LEVEL_PROVIDER == PlayerLevelType.ADVENTURE) {
            PlayerLevelIntegration.setImplementation(AdventurePlayerLevelIntegration)
            PlayerDataLoadingCoordinator.registerExternalStage2Handler("AdventureLevel")
            ResourceLoadingFixHandler.setImplementation(AdventureResourceLoadingFixHandler)
        }
    }
}

private object AdventureResourceLoadingFixHandler : ResourceLoadingFixHandler {

    override fun fix() {

        event<AdventureLevelDataLoadEvent> { event ->
            val data = event.userData
            val player = Bukkit.getServer().getPlayer(data.uuid) ?: run {
                LOGGER.warn("Player ${data.uuid} is not online, skipping resource synchronization")
                return@event
            }
            PlayerDataLoadingCoordinator.getOrCreateSession(player).completeStage2()
        }
    }
}

private object AdventurePlayerLevelIntegration : PlayerLevelIntegration {

    override val type: PlayerLevelType = PlayerLevelType.ADVENTURE

    override fun get(uuid: UUID): Int? {
        val api = AdventureLevelProvider.get()
        val userDataRepository = api.userDataRepository
        val userData = userDataRepository.getCached(uuid) ?: return null
        val primaryLevel = userData.getLevel(LevelCategory.PRIMARY)
        return primaryLevel.level
    }
}

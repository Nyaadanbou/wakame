package cc.mewcraft.wakame.level

import cc.mewcraft.adventurelevel.level.category.LevelCategory
import cc.mewcraft.adventurelevel.plugin.AdventureLevelProvider
import java.util.UUID

/**
 * Gets the adventure level from specific player.
 */
class AdventureLevelProvider : PlayerLevelProvider {

    override fun get(uuid: UUID): Int? {
        val api = AdventureLevelProvider.get()
        val playerDataManager = api.playerDataManager()
        val playerData = playerDataManager.load(uuid)

        if (!playerData.complete()) {
            return null
        }

        val primaryLevel = playerData.getLevel(LevelCategory.PRIMARY)
        return primaryLevel.level
    }

}
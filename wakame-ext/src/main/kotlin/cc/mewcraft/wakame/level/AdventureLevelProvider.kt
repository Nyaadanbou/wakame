package cc.mewcraft.wakame.level

import java.util.UUID

/**
 * Gets the adventure level from specific player.
 */
class AdventureLevelProvider : PlayerLevelProvider {

    override fun get(uuid: UUID): Int? {
        return 14 // TODO implement the compatibility with AdventureLevel plugin
    }

}
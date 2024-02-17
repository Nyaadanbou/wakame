package cc.mewcraft.wakame.level

import cc.mewcraft.wakame.annotation.InternalApi
import java.util.UUID

/**
 * Gets the adventure level from specific player.
 */
@InternalApi
class CustomAdventureLevelGetter : PlayerLevelGetter {

    override fun get(uuid: UUID): Int? {
        return 14 // TODO implement the compatibility with AdventureLevel plugin
    }

}
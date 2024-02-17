package cc.mewcraft.wakame.level

import cc.mewcraft.wakame.annotation.InternalApi
import org.bukkit.Server
import java.util.UUID

/**
 * Gets the [vanilla experience level](https://minecraft.wiki/w/Experience)
 * from specific player.
 */
@InternalApi
class VanillaExperienceLevelGetter(
    private val server: Server,
) : PlayerLevelGetter {

    override fun get(uuid: UUID): Int? {
        return server.getPlayer(uuid)?.level
    }
}
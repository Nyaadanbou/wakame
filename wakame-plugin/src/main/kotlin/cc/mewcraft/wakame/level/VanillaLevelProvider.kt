package cc.mewcraft.wakame.level

import org.bukkit.Server
import java.util.UUID

/**
 * Gets the [vanilla experience level](https://minecraft.wiki/w/Experience)
 * from specific player.
 */
class VanillaLevelProvider(
    private val server: Server,
) : PlayerLevelProvider {
    override fun get(uuid: UUID): Int? {
        return server.getPlayer(uuid)?.level
    }
}
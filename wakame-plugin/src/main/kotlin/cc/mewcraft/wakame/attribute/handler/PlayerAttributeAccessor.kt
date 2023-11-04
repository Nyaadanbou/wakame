package cc.mewcraft.wakame.attribute.handler

import me.lucko.helper.plugin.HelperPlugin
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import java.util.*

/**
 * Provides access to [PlayerAttributeMap] of a player. Not thread-safe.
 */
class PlayerAttributeAccessor(
    private val plugin: HelperPlugin,
) : Listener {
    private val cache = HashMap<UUID, PlayerAttributeMap>()

    fun getAttributeMap(uuid: UUID): PlayerAttributeMap {
        return cache.computeIfAbsent(uuid) { PlayerAttributeMap() }
    }

    fun getAttributeMap(player: Player): PlayerAttributeMap {
        return getAttributeMap(player.uniqueId)
    }
}
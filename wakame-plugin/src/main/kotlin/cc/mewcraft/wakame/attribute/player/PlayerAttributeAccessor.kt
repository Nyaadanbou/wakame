package cc.mewcraft.wakame.attribute.player

import cc.mewcraft.wakame.attribute.AttributeMap
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Provides access to [AttributeMap] of a player.
 */
class PlayerAttributeAccessor : Listener {
    private val cache = ConcurrentHashMap<UUID, AttributeMap>()

    fun getAttributeMap(playerUuid: UUID): AttributeMap {
        return cache.computeIfAbsent(playerUuid) { AttributeMap() }
    }

    /**
     * Starts to periodically calculate AttributeMap of all online players.
     */
    fun initialize() {
        TODO()
    }

    // --- Listeners ---

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        e.player.inventory.run {

        }
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {

    }

    @EventHandler
    fun onEquip(e: PlayerArmorChangeEvent) {

    }
}
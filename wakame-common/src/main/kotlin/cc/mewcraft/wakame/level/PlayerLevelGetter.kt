package cc.mewcraft.wakame.level

import org.bukkit.OfflinePlayer
import java.util.UUID

interface PlayerLevelGetter {

    /**
     * Gets the player's level from the player's UUID.
     *
     * @param uuid the UUID of the player
     * @return the level of the player
     */
    fun get(uuid: UUID): Int?

    /**
     * Gets the player's level from the player.
     *
     * @param player the player
     * @return the level of the player
     */
    fun get(player: OfflinePlayer): Int? {
        return get(player.uniqueId)
    }

    /**
     * @see getOrDefault
     */
    fun getOrDefault(uuid: UUID, def: Int): Int {
        return get(uuid) ?: def
    }

    /**
     * @see getOrDefault
     */
    fun getOrDefault(player: OfflinePlayer, def: Int): Int {
        return get(player) ?: def
    }

}
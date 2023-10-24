package cc.mewcraft.wakame.attribute

import java.util.*

/**
 * Provides access to [AttributeMap] of a player.
 */
interface PlayerAttributeCache {
    fun getAttributeMap(player: UUID): AttributeMap {
        TODO()
    }
}
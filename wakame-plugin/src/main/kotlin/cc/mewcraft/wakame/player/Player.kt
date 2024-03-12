package cc.mewcraft.wakame.player

import cc.mewcraft.wakame.attribute.AttributeMap
import cc.mewcraft.wakame.kizami.KizamiMap
import java.util.UUID

/**
 * Represents a player in wakame system.
 */
interface Player {

    /**
     * The backing player.
     */
    val player: Any

    /**
     * Unique identifier of the player.
     */
    val uniqueId: UUID

    /**
     * Level of the player.
     */
    val level: Int

    /**
     * Kizami Map of the player.
     */
    val kizamiMap: KizamiMap

    /**
     * Attribute Map of the player.
     */
    val attributeMap: AttributeMap

}
package cc.mewcraft.wakame.player

import cc.mewcraft.wakame.attribute.AttributeMap
import cc.mewcraft.wakame.kizami.KizamiMap

/**
 * Represents a player in wakame system.
 */
interface NekoPlayer {

    /**
     * Kizami Map of the player.
     */
    val kizamiMap: KizamiMap

    /**
     * Attribute Map of the player.
     */
    val attributeMap: AttributeMap

}
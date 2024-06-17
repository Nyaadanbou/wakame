package cc.mewcraft.wakame.user

import cc.mewcraft.wakame.attribute.AttributeMap
import cc.mewcraft.wakame.kizami.KizamiMap
import cc.mewcraft.wakame.resource.ResourceMap
import cc.mewcraft.wakame.skill.SkillMap
import cc.mewcraft.wakame.skill.state.SkillState
import java.util.UUID

/**
 * Represents a player in wakame system.
 *
 * @param P the player type
 */
interface User<P> {

    /**
     * The backing player.
     */
    val player: P

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

    /**
     * Skill Map of the player.
     */
    val skillMap: SkillMap

    /**
     * Resource Map of the player.
     */
    val resourceMap: ResourceMap

    /**
     * Skill State Manager of the player.
     */
    val skillState: SkillState
}
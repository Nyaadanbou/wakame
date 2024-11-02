package cc.mewcraft.wakame.user

import cc.mewcraft.wakame.attribute.AttributeMap
import cc.mewcraft.wakame.kizami.KizamiMap
import cc.mewcraft.wakame.player.attackspeed.AttackSpeed
import cc.mewcraft.wakame.resource.ResourceMap
import cc.mewcraft.wakame.skill.SkillMap
import cc.mewcraft.wakame.skill.state.SkillState
import java.util.UUID

/**
 * Represents a player in wakame system.
 *
 * 这个类**不能**被其他类持有, 只能通过 [UserManager] 或 [PlayerAdapters] 获取.
 *
 * @param P the player type
 */
interface User<P> {

    /**
     * The backing player.
     */
    val player: P

    /**
     * UUID of the player.
     */
    val uniqueId: UUID

    /**
     * Main Level of the player.
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
    val skillState: SkillState<P>

    /**
     * Attack Speed of the player.
     */
    val attackSpeed: AttackSpeed

    /**
     * 关闭跟本实例相关的资源, 例如停止运行中的技能.
     */
    fun cleanup()
}
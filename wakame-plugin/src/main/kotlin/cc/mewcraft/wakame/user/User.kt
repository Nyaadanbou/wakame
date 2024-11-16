package cc.mewcraft.wakame.user

import cc.mewcraft.wakame.attribute.AttributeMap
import cc.mewcraft.wakame.kizami.KizamiMap
import cc.mewcraft.wakame.player.attackspeed.AttackSpeed
import cc.mewcraft.wakame.resource.ResourceMap
import cc.mewcraft.wakame.skill.SkillMap
import cc.mewcraft.wakame.skill.state.SkillState
import java.util.UUID

/**
 * Represents a user in the system.
 *
 * 不要永久持有本接口的对象, 以免造成资源泄漏!
 * 要获取该对象, 使用 [UserManager.getUser].
 *
 * @param P the player type
 */
interface User<P> {

    /**
     * The backing player.
     */
    val player: P

    /**
     * The UUID of the user.
     * This is the same as the UUID of the player.
     */
    val uniqueId: UUID

    /**
     * The Main Level of the player.
     */
    val level: Int

    /**
     * The Kizami Map of the player.
     */
    val kizamiMap: KizamiMap

    /**
     * The Attribute Map of the player.
     */
    val attributeMap: AttributeMap

    /**
     * The Skill Map of the player.
     */
    val skillMap: SkillMap

    /**
     * The Resource Map of the player.
     */
    val resourceMap: ResourceMap

    /**
     * The Skill State Manager of the player.
     */
    val skillState: SkillState<P>

    /**
     * The Attack Speed of the player.
     */
    val attackSpeed: AttackSpeed

    /**
     * 标记系统是否应该监听该玩家的背包变化.
     *
     * 当该值为 `false` 时, 系统不应该监听背包变化.
     * 这意味着玩家背包里的任何物品都相当于原版物品, 不会提供任何来自萌芽的特殊效果 (例如属性加成).
     * 当该值为 `true` 时, 系统应该监听背包变化.
     * 此时如果玩家背包里存在萌芽物品, 系统应该将这些物品的效果提供给玩家, 或从玩家身上移除.
     */
    var isInventoryListenable: Boolean

    /**
     * 关闭跟本实例相关的资源, 例如停止运行中的技能.
     */
    fun cleanup()
}
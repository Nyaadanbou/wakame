package cc.mewcraft.wakame.event.bukkit

import cc.mewcraft.wakame.attribute.AttributeMapSnapshot
import cc.mewcraft.wakame.user.toUser
import org.bukkit.damage.DamageSource
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * 该事件发生在 [cc.mewcraft.wakame.attribute.AttributeMap] 被传递到伤害系统的计算逻辑之前.
 * 监听器可通过该事件来修改 [cc.mewcraft.wakame.attribute.AttributeMap] 以影响最终的伤害结果.
 *
 * 经讨论, 只有玩家触发的伤害需要通过该事件来修改, 因此该事件始终包含一个 [Player] 类型的 [damager].
 *
 * ### 其他
 * 值得一提的是, 伤害系统在设计之初就不支持直接修改最终的伤害值. 所有关于伤害的修改都必须通过属性来实现.
 * 如此设计可以让不同系统(如技能,附魔,武器效果)对伤害的修饰处于同一框架之内, 使得每个修饰都可被精确描述.
 *
 * @param damager 造成伤害的玩家
 * @param damagee 受到伤害的实体. 当打出的伤害无法在当前 tick 结算时为 `null`
 * @param damageSource 伤害来源, 包含更多伤害信息
 */
// FIXME #366: 仍然需要 directEntity, causingEntity.
//  DamageSource 在刚射出箭矢时是不存在的, 但
//  shooter(=causingEntity) 和 arrow(=directEntity)
//  是肯定存在的.
// FIXME #366: 让 preprocess 支持 cancel 以尽早的停止伤害计算节省资源?
class NekoPreprocessEntityDamageEvent
internal constructor(
    val damager: Player,
    val damagee: LivingEntity?,
    val damageSource: DamageSource?,
) : Event() {

    // 造成伤害的实体永远是玩家, 所以 attributes 永远存在
    val damagerAttributes: AttributeMapSnapshot =
        damager.toUser().attributeMap.getSnapshot()

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }
}

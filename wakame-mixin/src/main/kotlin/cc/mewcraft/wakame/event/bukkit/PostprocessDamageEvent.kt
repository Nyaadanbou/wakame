package cc.mewcraft.wakame.event.bukkit

import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.DefenseMetadata
import cc.mewcraft.wakame.damage.FinalDamageContext
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import org.bukkit.damage.DamageSource
import org.bukkit.entity.Entity
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityDamageEvent

/**
 * 该事件发生在最终伤害已经计算完毕, 但还未实际将最终伤害应用到实体上.
 *
 * - 监听该事件可以读取到完整的伤害信息 (攻击阶段/防御阶段).
 * - 取消该事件会使本次伤害失效.
 * - 无法使用该事件修改伤害.
 * - 伤害的所有计算逻辑均应由伤害系统负责, 不提供外部修改的接口.
 *
 */
class PostprocessDamageEvent(
    val finalDamageContext: FinalDamageContext,
    private val bukkitEvent: EntityDamageEvent,
) : Event(), Cancellable {

    /**
     * 受伤的实体.
     */
    val damagee: Entity
        get() = bukkitEvent.entity

    /**
     * 伤害来源.
     * 可能是坐标也可能是实体.
     */
    val damageSource: DamageSource
        get() = bukkitEvent.damageSource

    /**
     * 攻击阶段的伤害信息.
     */
    val damageMetadata: DamageMetadata
        get() = finalDamageContext.damageMetadata

    /**
     * 防御阶段的伤害信息.
     */
    val defenseMetadata: DefenseMetadata
        get() = finalDamageContext.defenseMetadata


    /**
     * 获取本次伤害事件的最终伤害的值 (即各元素的最终伤害的简单相加).
     */
    val finalDamage: Double
        get() = finalDamageContext.finalDamageMap.values.sum()

    /**
     * 获取一个包含了每种元素的最终伤害值的映射.
     */
    val finalDamageMap: Map<RegistryEntry<Element>, Double>
        get() = finalDamageContext.finalDamageMap

    override fun isCancelled(): Boolean {
        return bukkitEvent.isCancelled
    }

    override fun setCancelled(cancel: Boolean) {
        bukkitEvent.isCancelled = cancel
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = handlerList
    }

}
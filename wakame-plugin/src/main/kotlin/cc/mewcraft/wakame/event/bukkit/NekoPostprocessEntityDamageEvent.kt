package cc.mewcraft.wakame.event.bukkit

import cc.mewcraft.wakame.damage.CriticalStrikeState
import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.element.ElementType
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap
import org.bukkit.damage.DamageSource
import org.bukkit.entity.Entity
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent

@Deprecated("该事件的名字有点模糊", replaceWith = ReplaceWith("NekoPostprocessEntityDamageEvent"))
typealias NekoEntityDamageEvent = NekoPostprocessEntityDamageEvent

/**
 * @property damageMetadata 伤害信息 (计算防御前)
 * @property finalDamageMap 伤害信息 (计算防御后)
 */
class NekoPostprocessEntityDamageEvent
internal constructor(
    val damageMetadata: DamageMetadata,
    private val finalDamageMap: Reference2DoubleMap<RegistryEntry<ElementType>>,
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
     * 获取本次伤害是否是玩家跳劈.
     */
    fun isJumpCriticalHit(): Boolean {
        return bukkitEvent is EntityDamageByEntityEvent && bukkitEvent.isCritical
    }

    /**
     * 获取本次伤害事件中指定元素的最终伤害值. 若元素不存在则返回 `null`.
     */
    fun getFinalDamage(element: RegistryEntry<ElementType>): Double? {
        if (!finalDamageMap.containsKey(element)) return null
        return finalDamageMap.getDouble(element)
    }

    /**
     * 获取本次伤害事件的最终伤害的值 (即各元素的最终伤害的简单相加).
     */
    fun getFinalDamage(): Double {
        return finalDamageMap.values.sum()
    }

    /**
     * 获取一个包含了每种元素的最终伤害值的映射.
     */
    fun getFinalDamageMap(): Map<RegistryEntry<ElementType>, Double> {
        return finalDamageMap
    }

    /**
     * 获取本次伤害的暴击状态.
     */
    fun getCriticalState(): CriticalStrikeState {
        return damageMetadata.criticalStrikeMetadata.state
    }

    /**
     * 获取本次伤害的暴击倍率.
     */
    fun getCriticalPower(): Double {
        return damageMetadata.criticalStrikeMetadata.power
    }

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
package cc.mewcraft.wakame.event.bukkit

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.damage.CriticalStrikeState
import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.DefenseMetadata
import cc.mewcraft.wakame.element.ElementType
import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap
import org.bukkit.damage.DamageSource
import org.bukkit.entity.Entity
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent

class NekoEntityDamageEvent(
    val damageMetadata: DamageMetadata,
    val defenseMetadata: DefenseMetadata,
    private val bukkitEvent: EntityDamageEvent,
) : Event(), Cancellable {

    /**
     * 记录了每种伤害在计算防御后的最终数值.
     */
    private val finalDamagePerElement: Reference2DoubleOpenHashMap<ElementType> = Reference2DoubleOpenHashMap()

    init {
        val damagePackets = damageMetadata.damageBundle.packets()
        if (damagePackets.isEmpty()) {
            // 记录空伤害包以方便定位问题
            LOGGER.warn("Empty damage bundle!")
        } else {
            damagePackets.forEach { packet ->
                val element = packet.element
                val damage = defenseMetadata.calculateFinalDamage(element, damageMetadata)
                if (damage > 0) {
                    finalDamagePerElement[element] = damage
                }
            }
        }
    }

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
     * 获取本次伤害事件中指定元素的最终伤害值. 若元素不存在则返回 null.
     */
    fun getFinalDamage(element: ElementType): Double? {
        if (!finalDamagePerElement.containsKey(element)) {
            return null
        }
        return finalDamagePerElement.getDouble(element)
    }

    /**
     * 获取本次伤害事件的最终伤害的值 (即各元素的最终伤害的简单相加).
     */
    fun getFinalDamage(): Double {
        return finalDamagePerElement.values.sumOf { it }
    }

    /**
     * 获取一个包含了每种元素的最终伤害值的映射.
     */
    fun getFinalDamageMap(): Map<ElementType, Double> {
        return finalDamagePerElement
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
        return HANDLER_LIST
    }

    companion object {
        @JvmStatic
        val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLER_LIST
    }
}
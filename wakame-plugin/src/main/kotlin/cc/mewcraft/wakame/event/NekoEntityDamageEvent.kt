package cc.mewcraft.wakame.event

import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.DefenseMetadata
import cc.mewcraft.wakame.element.Element
import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap
import org.bukkit.damage.DamageSource
import org.bukkit.entity.Entity
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityDamageEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.slf4j.Logger

class NekoEntityDamageEvent(
    val damageSource: DamageSource,//TODO 移除
    val damageMetadata: DamageMetadata,
    val defenseMetadata: DefenseMetadata,
    val bukkitEvent: EntityDamageEvent
) : Event(), Cancellable, KoinComponent {
    private var cancel: Boolean = false

    /**
     * 记录了每种伤害在计算防御后的最终数值.
     */
    private val finalDamagePerElement: Reference2DoubleOpenHashMap<Element> = Reference2DoubleOpenHashMap()

    init {
        val damagePackets = damageMetadata.damageBundle.packets()
        if (damagePackets.isEmpty()) {
            // 记录空伤害包以方便定位问题
            get<Logger>().warn("Empty damage bundle!")
        } else {
            damagePackets.forEach { packet ->
                val element = packet.element
                val damage = defenseMetadata.calculateFinalDamage(element, damageMetadata)
                finalDamagePerElement[element] = damage
            }
        }
    }

    /**
     * 受伤的实体.
     */
    val damagee: Entity
        get() = bukkitEvent.entity

    /**
     * 查询本次事件中是否含有指定元素类型的伤害.
     */
    fun hasElement(element: Element): Boolean {
        return damageMetadata.damageBundle.get(element) != null
    }

    /**
     * 获取本次伤害事件中指定元素的最终伤害值. 若元素不存在则返回 0.0.
     */
    fun getFinalDamage(element: Element): Double {
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
     * 注意, 返回的映射包含数值为零的伤害.
     * TODO 有关0伤害的处理
     */
    fun getFinalDamageMap(excludeZeroDamage: Boolean = false): Map<Element, Double> {
        return if (excludeZeroDamage) {
            finalDamagePerElement.filter { it.value > 0.0 }
        } else {
            finalDamagePerElement
        }
    }

    /**
     * 获取本次伤害是否为正暴击, 未暴击将返回 `false`.
     * 暴击了但伤害未变化 (即暴击倍率为 `1.0` 时) 算未暴击.
     */
    fun isPositiveCriticalStrike(): Boolean {
        return damageMetadata.isCritical && damageMetadata.criticalPower > 1
    }

    /**
     * 获取本次伤害是否为负暴击, 未暴击将返回 `false`.
     * 暴击了但伤害未变化 (即暴击倍率为 `1.0` 时) 算未暴击.
     */
    fun isNegativeCriticalStrike(): Boolean {
        return damageMetadata.isCritical && damageMetadata.criticalPower < 1
    }

    override fun isCancelled(): Boolean {
        return cancel || bukkitEvent.isCancelled
    }

    override fun setCancelled(cancel: Boolean) {
        this.cancel = cancel
        bukkitEvent.isCancelled = cancel
    }

    override fun getHandlers(): HandlerList = HANDLER_LIST

    companion object {
        @JvmStatic
        val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLER_LIST
    }
}
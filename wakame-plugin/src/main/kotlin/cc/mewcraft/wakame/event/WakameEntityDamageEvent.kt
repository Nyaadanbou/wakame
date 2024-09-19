package cc.mewcraft.wakame.event

import cc.mewcraft.wakame.damage.DamageMetadata
import cc.mewcraft.wakame.damage.DefenseMetadata
import cc.mewcraft.wakame.element.Element
import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap
import org.bukkit.damage.DamageSource
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

class WakameEntityDamageEvent(
    val damageSource: DamageSource,
    val damageMetadata: DamageMetadata,
    val defenseMetadata: DefenseMetadata
) : Event(), Cancellable, KoinComponent {
    private val logger: Logger by inject()
    private var cancel: Boolean = false
    private val elementFinalDamageMap: Reference2DoubleOpenHashMap<Element>

    init {
        val damagePackets = damageMetadata.damageBundle.packets()
        if (damagePackets.isEmpty()) {
            // 对空伤害包进行警告
            logger.warn("Empty Damage Bundle!")
            elementFinalDamageMap = Reference2DoubleOpenHashMap()
        } else {
            elementFinalDamageMap = Reference2DoubleOpenHashMap(
                damagePackets.associate {
                    it.element to defenseMetadata.calculateFinalDamage(it.element, damageMetadata)
                }
            )
        }
    }

    /**
     * 本次事件中是否含有某元素类型的伤害.
     */
    fun hasElement(element: Element): Boolean {
        return damageMetadata.damageBundle.get(element) != null
    }

    /**
     * 获取本次伤害事件中某一元素的最终伤害的值.
     * 若元素不存在则返回0.
     */
    fun getFinalDamage(element: Element): Double {
        return elementFinalDamageMap.getDouble(element)
    }

    /**
     * 获取本次伤害事件的最终伤害的值.
     * 是各元素伤害的简单相加.
     */
    fun getFinalDamage(): Double {
        return elementFinalDamageMap.values.sumOf { it }
    }

    override fun isCancelled(): Boolean {
        return cancel
    }

    override fun setCancelled(cancel: Boolean) {
        this.cancel = cancel
    }

    override fun getHandlers(): HandlerList = HANDLER_LIST

    companion object {
        @JvmStatic
        val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLER_LIST
    }
}
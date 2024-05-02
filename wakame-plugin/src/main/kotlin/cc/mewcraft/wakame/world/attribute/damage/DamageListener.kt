package cc.mewcraft.wakame.world.attribute.damage

import cc.mewcraft.wakame.event.WakameEntityDamageEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class DamageListener : Listener {
    @EventHandler
    fun on(event: EntityDamageEvent) {
        val damageMetaData = DamageManager.generateMetaData(event)
        val defenseMetaData = DamageManager.generateDefenseMetaData(event, damageMetaData, emptyList())
        val wakameEntityDamageEvent = WakameEntityDamageEvent(event, damageMetaData, defenseMetaData)
        wakameEntityDamageEvent.callEvent()

        //如果伤害事件被取消，什么也不做
        //WakameEntityDamageEvent的取消与否完全依赖于其内部的EntityDamageEvent
        if (wakameEntityDamageEvent.isCancelled) return


    }
}
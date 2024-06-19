package cc.mewcraft.wakame.world.attribute.damage

import cc.mewcraft.wakame.event.WakameEntityDamageEvent
import cc.mewcraft.wakame.user.toUser
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.SpectralArrow
import org.bukkit.entity.Trident
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.projectiles.BlockProjectileSource

class DamageListener : Listener {
    @EventHandler
    fun on(event: EntityDamageEvent) {
        val damageMetaData = DamageManager.generateDamageMetaData(event)
        val defenseMetaData = DamageManager.generateDefenseMetaData(event)
        val wakameEntityDamageEvent = WakameEntityDamageEvent(event, damageMetaData, defenseMetaData)
        wakameEntityDamageEvent.callEvent()

        //如果伤害事件被取消，什么也不做
        //WakameEntityDamageEvent的取消与否完全依赖于其内部的EntityDamageEvent
        if (wakameEntityDamageEvent.isCancelled) return


        //修改最终伤害
        event.damage = defenseMetaData.calculateFinalDamage(damageMetaData)
    }

    /**
     * 在弹射物射出时记录其 [DamageMetaData]
     */
    @EventHandler
    fun on(event: ProjectileLaunchEvent) {
        DamageManager.recordProjectileDamageMetaData(event)
    }

}
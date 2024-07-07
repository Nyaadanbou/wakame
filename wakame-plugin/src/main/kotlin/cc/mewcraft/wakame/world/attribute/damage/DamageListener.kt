package cc.mewcraft.wakame.world.attribute.damage

import cc.mewcraft.wakame.event.WakameEntityDamageEvent
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.AbstractArrow
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

class DamageListener : Listener, KoinComponent {
    private val logger: Logger by inject()

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
        logger.info("${event.entity.type}(${event.entity.uniqueId}) 受到了 ${event.damage} 点伤害")
        Bukkit.broadcast(Component.text("${event.entity.type}(${event.entity.uniqueId}) 受到了 ${event.damage} 点伤害"))
    }

    /**
     * 在弹射物射出时记录其 [DamageMetaData]
     */
    @EventHandler
    fun on(event: ProjectileLaunchEvent) {
        DamageManager.recordProjectileDamageMetaData(event)
    }

    /**
     * 在弹射物击中方块时移除记录的 [DamageMetaData]
     */
    @EventHandler
    fun on(event: ProjectileHitEvent) {
        if (event.hitBlock == null) {
            return
        }
        when (val projectile = event.entity) {
            //弹射物是箭矢（普通箭、光灵箭、药水箭）、三叉戟
            is AbstractArrow -> {
                DamageManager.removeProjectileDamageMetaData(projectile.uniqueId)
            }

            //TODO 可能还会有其他需要wakame属性系统处理的弹射物
        }
    }
}
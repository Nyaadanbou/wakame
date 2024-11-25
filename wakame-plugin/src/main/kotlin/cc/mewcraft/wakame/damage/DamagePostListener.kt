@file:Suppress(
    "UnstableApiUsage"
)

package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.event.NekoEntityDamageEvent
import cc.mewcraft.wakame.integration.protection.ProtectionManager
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

/**
 * 监听萌芽伤害事件, 使其遵循保护系统的规则.
 */
object DamagePostListener : Listener {

    @EventHandler(
        ignoreCancelled = true,
        priority = EventPriority.HIGHEST
    )
    fun on(event: NekoEntityDamageEvent) {
        val damager = event.damageSource.causingEntity as? Player ?: return
        val damagee = event.damagee as? LivingEntity ?: return

        event.isCancelled = !ProtectionManager.canHurtEntity(damager, damagee, null)
    }

}
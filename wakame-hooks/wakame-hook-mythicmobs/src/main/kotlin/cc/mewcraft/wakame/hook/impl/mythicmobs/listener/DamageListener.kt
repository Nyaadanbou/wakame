package cc.mewcraft.wakame.hook.impl.mythicmobs.listener

import cc.mewcraft.wakame.integration.protection.ProtectionManager
import io.lumine.mythic.bukkit.events.MythicDamageEvent
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object DamageListener : Listener {

    // SkillAdapter#doDamage 会触发该事件, 我们必须在这个事件里修改结果使其遵循领地和保护区的规则.
    // Bukkit 的 EntityDamageEvent 也会被触发, 但那个事件里的 DamageSource 完全没法正常使用.
    // TODO 现在没用到 SkillAdapter#doDamage 了, 还需要这个监听器吗?
    @EventHandler
    fun on(event: MythicDamageEvent) {
        val damager = event.caster.entity.bukkitEntity as? Player ?: return
        val victim = event.target.bukkitEntity as? LivingEntity ?: return
        event.isCancelled = !ProtectionManager.canHurtEntity(damager, victim, null)
    }
}
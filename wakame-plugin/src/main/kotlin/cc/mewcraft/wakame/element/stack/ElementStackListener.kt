package cc.mewcraft.wakame.element.stack

import cc.mewcraft.wakame.ability.character.CasterAdapter
import cc.mewcraft.wakame.event.NekoEntityDamageEvent
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ElementStackListener : Listener {
    @EventHandler
    private fun onEntityDamage(event: NekoEntityDamageEvent) {
        if (event.isCancelled)
            return
        val damagee = event.damagee as? LivingEntity ?: return
        val damagePackets = event.damageMetadata.damageBundle.packets()
        for (damagePacket in damagePackets) {
            val caster = (event.damageSource.causingEntity as? LivingEntity)?.let { CasterAdapter.adapt(it) }
            damagee.applyElementStack(damagePacket.element, 1, caster)
        }
    }
}
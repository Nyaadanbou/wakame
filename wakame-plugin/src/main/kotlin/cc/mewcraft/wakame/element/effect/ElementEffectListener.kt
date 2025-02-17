package cc.mewcraft.wakame.element.effect

import cc.mewcraft.wakame.event.NekoEntityDamageEvent
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ElementEffectListener : Listener {
    @EventHandler
    private fun onEntityDamage(event: NekoEntityDamageEvent) {
        if (event.isCancelled)
            return
        val damagee = event.damagee as? LivingEntity ?: return
        val damagePackets = event.damageMetadata.damageBundle.packets()
        for (damagePacket in damagePackets) {
            val elementEffect = ElementEffect(RegistryEntry.direct(damagePacket.element))
            elementEffect.apply(event.damageSource.causingEntity as? LivingEntity, damagee)
        }
    }
}
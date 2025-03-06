package cc.mewcraft.wakame.element.stack

import cc.mewcraft.wakame.ecs.bridge.toKoish
import cc.mewcraft.wakame.event.bukkit.NekoEntityDamageEvent
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.event
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Listener

@Init(
    stage = InitStage.POST_WORLD,
)
object ElementStackListener : Listener {
    @InitFun
    private fun init() {
        registerEvents()
    }

    private fun registerEvents() {
        event<NekoEntityDamageEvent> { event ->
            if (event.isCancelled)
                return@event
            val damagee = event.damagee as? LivingEntity ?: return@event
            val damagePackets = event.damageMetadata.damageBundle.packets()
            for (damagePacket in damagePackets) {
                val caster = (event.damageSource.causingEntity as? LivingEntity)?.toKoish()
                damagee.applyElementStack(damagePacket.element, 1, caster)
            }
        }
    }
}
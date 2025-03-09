package cc.mewcraft.wakame.elementstack

import cc.mewcraft.wakame.event.bukkit.NekoEntityDamageEvent
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.event
import org.bukkit.event.Listener

@Init(
    stage = InitStage.POST_WORLD,
)
object ElementStackListener : Listener {
    @InitFun
    fun init() {
        registerEvents()
    }

    private fun registerEvents() {
        event<NekoEntityDamageEvent> { event ->
            if (event.isCancelled)
                return@event
            val damagee = event.damagee
            val damagePackets = event.damageMetadata.damageBundle.packets()
            for (damagePacket in damagePackets) {
                val causingEntity = event.damageSource.causingEntity
                damagee.applyElementStack(damagePacket.element, 1, causingEntity)
            }
        }
    }
}
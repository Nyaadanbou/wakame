package cc.mewcraft.wakame.entity.attribute.source

import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.registerEvents
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPotionEffectEvent

@Init(InitStage.POST_WORLD)
object AttributeSourceListener : Listener {

    @InitFun
    fun init() {
        registerEvents()
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPotionEffect(event: EntityPotionEffectEvent) {
        val livingEntity = event.entity as? LivingEntity ?: return
        val oldEffect = event.oldEffect
        if (oldEffect != null) {
            AttributeSourceRegistry.byEffectType(oldEffect.type)?.forEach {
                it.remove(livingEntity)
            }
        }
        val newEffect = event.newEffect
        if (newEffect != null) {
            AttributeSourceRegistry.byEffectType(newEffect.type)?.forEach {
                it.apply(livingEntity)
            }
        }
    }
}
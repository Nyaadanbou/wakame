package cc.mewcraft.wakame.hook.impl.mythicmobs.listener

import cc.mewcraft.wakame.hook.impl.mythicmobs.placeholder.KoishPlaceholders
import cc.mewcraft.wakame.mixin.support.EntityTypeWrapperObjects
import io.lumine.mythic.bukkit.events.MythicReloadCompleteEvent
import io.lumine.mythic.bukkit.events.MythicReloadedEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object ReloadListener : Listener {

    @EventHandler
    fun on(event: MythicReloadedEvent) {
        KoishPlaceholders.register(event.instance)
    }

    @EventHandler
    fun on(event: MythicReloadCompleteEvent) {
        EntityTypeWrapperObjects.resetDelegates()
    }
}
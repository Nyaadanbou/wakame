package cc.mewcraft.wakame.hook.impl.mythicmobs.listener

import cc.mewcraft.wakame.mixin.support.EntityTypeWrapperObjects
import io.lumine.mythic.bukkit.events.MythicReloadCompleteEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object ReloadListener : Listener {

    @EventHandler
    fun on(event: MythicReloadCompleteEvent) {
        EntityTypeWrapperObjects.reloadInstances()
    }
}
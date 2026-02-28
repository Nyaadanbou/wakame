package cc.mewcraft.wakame.hook.impl.mythicmobs.listener

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.hook.impl.mythicmobs.placeholder.KoishPlaceholders
import cc.mewcraft.wakame.mixin.support.EntityTypeWrapperObjects
import io.lumine.mythic.bukkit.events.MythicReloadCompleteEvent
import io.lumine.mythic.bukkit.events.MythicReloadEvent
import io.lumine.mythic.bukkit.events.MythicReloadedEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object ReloadListener : Listener {

    @EventHandler
    fun on(event: MythicReloadEvent) {
        KoishPlaceholders.register(event.instance)
        LOGGER.info("Registered KoishPlaceholders for MythicMobs")
    }

    @EventHandler
    fun on(event: MythicReloadedEvent) {
        KoishPlaceholders.register(event.instance)
        LOGGER.info("Registered KoishPlaceholders for MythicMobs")
    }

    @EventHandler
    fun on(event: MythicReloadCompleteEvent) {
        KoishPlaceholders.register(event.instance)
        LOGGER.info("Registered KoishPlaceholders for MythicMobs")
        EntityTypeWrapperObjects.resetDelegates()
        LOGGER.info("Reset EntityTypeWrapper delegates for datapacks")
    }
}
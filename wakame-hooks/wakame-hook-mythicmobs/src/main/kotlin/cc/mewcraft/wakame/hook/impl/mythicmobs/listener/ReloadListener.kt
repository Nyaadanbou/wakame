package cc.mewcraft.wakame.hook.impl.mythicmobs.listener

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.mixin.support.EntityTypeWrapperObjects
import cc.mewcraft.wakame.mixin.support.MythicBootstrapBridge
import io.lumine.mythic.bukkit.events.MythicReloadCompleteEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object ReloadListener : Listener {

    @EventHandler
    fun on(event: MythicReloadCompleteEvent) {
        updateEntityTypeWrapperStates()
    }

    private fun updateEntityTypeWrapperStates() {
        // 重新初始化 MythicBootstrapBridge
        MythicBootstrapBridge.init()

        // 更新所有 EntityTypeWrapper 的 delegate
        for (inst in EntityTypeWrapperObjects.instances()) {
            val id = inst.id
            val entityType = MythicBootstrapBridge.getEntityType(id) ?: run {
                LOGGER.warn("The MythicMobs entity '$id' has no corresponding EntityType")
                continue
            }
            inst.setDelegate(entityType)
        }
    }
}
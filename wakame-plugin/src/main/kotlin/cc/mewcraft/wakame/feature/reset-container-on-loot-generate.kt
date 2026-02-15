package cc.mewcraft.wakame.feature

import cc.mewcraft.lazyconfig.access.entryOrElse
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.LootGenerateEvent

class ResetContainerOnLootGenerate : Listener {

    private val resetContainerOnLootGenerate by FEATURE_CONFIG.entryOrElse(false, "reset_container_on_loot_generate")

    @EventHandler
    fun on(event: LootGenerateEvent) {
        if (resetContainerOnLootGenerate.not()) return
        if (event.isPlugin) return
        val holder = event.inventoryHolder ?: return
        holder.inventory.clear()
    }
}
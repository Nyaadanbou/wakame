package cc.mewcraft.wakame.player.equipment

import cc.mewcraft.wakame.util.giveItemStack
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.koin.core.component.KoinComponent

class ArmorChangeListener : KoinComponent, Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    private fun onArmorChange(event: ArmorChangeEvent) {
        if (!event.isCancelled)
            return
        if (event.action == ArmorChangeEvent.Action.UNEQUIP) {
            return
        }
        val player = event.player
        val currentArmor = event.current ?: return
        player.giveItemStack(currentArmor)
    }
}
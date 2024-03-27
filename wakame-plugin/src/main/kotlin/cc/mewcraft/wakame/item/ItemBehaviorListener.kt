package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.item.binary.NekoStackFactory
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

class ItemBehaviorListener : Listener {
    @EventHandler
    fun onItemInteract(event: PlayerInteractEvent) {
        val item = event.item ?: return
        val nekoStack = NekoStackFactory.wrap(item).takeIf { it.isNeko } ?: return
        nekoStack.scheme.behaviors.forEach { behavior ->
            behavior.handleInteract(event.player, item, event.action, event)
        }
    }
}
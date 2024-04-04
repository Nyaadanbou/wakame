package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.item.binary.NekoStackFactory
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

class ItemListener : Listener {
    @EventHandler
    fun onItemInteract(event: PlayerInteractEvent) {
        val item = event.item ?: return
        val nekoStack = NekoStackFactory.wrap(item).takeIf { it.isNeko } ?: return
        nekoStack.schema.behaviors.forEach { behavior ->
            behavior.handleInteract(event.player, item, event.action, event)
        }
    }

    @EventHandler
    fun onDamageEntity(event: EntityDamageByEntityEvent) {
        // TODO: 这是一个 POC，可能需要考虑背包内的所有 Neko 物品
        val damager = event.damager as? Player ?: return
        val item = damager.inventory.itemInMainHand
        val nekoStack = NekoStackFactory.by(item) ?: return
        nekoStack.schema.behaviors.forEach { behavior ->
            behavior.handleAttackEntity(damager, item, event.entity, event)
        }
    }
}
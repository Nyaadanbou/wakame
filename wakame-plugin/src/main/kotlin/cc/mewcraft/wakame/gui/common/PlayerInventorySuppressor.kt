package cc.mewcraft.wakame.gui.common

import cc.mewcraft.wakame.util.registerEvents
import cc.mewcraft.wakame.util.unregisterEvents
import org.bukkit.entity.Player
import org.bukkit.event.*
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent

/**
 * 用来限制玩家在打开菜单时操作自己的背包.
 */
class PlayerInventorySuppressor(
    private val viewer: Player,
) : Listener {

    companion object Shared {
        private val allowedInventoryActions: Set<InventoryAction> = setOf(
            InventoryAction.PICKUP_ALL,
            InventoryAction.PLACE_ALL,
            InventoryAction.PLACE_ONE,
        )
    }

    fun startListening() {
        registerEvents()
    }

    fun stopListening() {
        unregisterEvents()
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: InventoryClickEvent) {
        if (event.whoClicked != viewer) {
            return
        }

        if (event.action !in allowedInventoryActions) {
            event.result = Event.Result.DENY
        }
    }
}
package cc.mewcraft.wakame.hook.impl.chestsort

import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.item2.property.impl.ItemSlotRegistry
import cc.mewcraft.wakame.util.registerEvents
import de.jeff_media.chestsort.api.ChestSortEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryType

@Hook(plugins = ["ChestSort"])
object ChestSortHook : Listener {

    init {
        registerEvents()
    }

    @EventHandler(ignoreCancelled = true)
    fun on(event: ChestSortEvent) {
        val inventory = event.inventory
        if (inventory.type == InventoryType.PLAYER) {
            inventory.holder as? Player ?: return

            val slots = ItemSlotRegistry.extraItemSlots()
            for (slot in slots) {
                // 不整理饰品栏位
                event.setUnmovable(slot.index)
            }
        }
    }

}
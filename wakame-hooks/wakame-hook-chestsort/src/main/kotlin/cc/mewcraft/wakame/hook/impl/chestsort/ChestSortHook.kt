package cc.mewcraft.wakame.hook.impl.chestsort

import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.item.ItemSlotRegistry
import cc.mewcraft.wakame.util.registerEvents
import de.jeff_media.chestsort.api.ChestSortEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryType
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Hook(plugins = ["ChestSort"])
object ChestSortHook : KoinComponent, Listener {

    init {
        registerEvents()
    }

    private val itemSlotRegistry: ItemSlotRegistry by inject()

    @EventHandler(ignoreCancelled = true)
    fun on(event: ChestSortEvent) {
        val inventory = event.inventory
        if (inventory.type == InventoryType.PLAYER) {
            inventory.holder as? Player ?: return

            val slots = itemSlotRegistry.custom()
            for (slot in slots) {
                // 不整理饰品栏位
                event.setUnmovable(slot.slotIndex)
            }
        }
    }

}
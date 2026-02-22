package cc.mewcraft.wakame.feature

import cc.mewcraft.lazyconfig.access.entryOrElse
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.commons.provider.map
import xyz.xenondevs.commons.provider.mapEachNotNull

class EquipmentSlotsToKeepOnDeath : Listener {

    private val equipmentSlotsToKeepOnDeath by FEATURE_CONFIG
        .entryOrElse(emptyList<String>(), "equipment_slots_to_keep_on_death")
        .mapEachNotNull(EquipmentSlotGroup::getByName)
        .map(List<EquipmentSlotGroup>::distinct)

    @EventHandler(priority = EventPriority.LOW)
    fun on(event: PlayerDeathEvent) {
        if (event.keepInventory) return
        if (equipmentSlotsToKeepOnDeath.isEmpty()) return

        val player = event.player
        val inventory = player.inventory
        val itemsToKeep = mutableListOf<ItemStack>()
        for (group in equipmentSlotsToKeepOnDeath) {
            for (slot in EquipmentSlot.entries) {
                if (group.test(slot)) {
                    itemsToKeep += inventory.getItem(slot)
                }
            }
        }
        for (item in itemsToKeep) {
            event.drops.remove(item)
            event.itemsToKeep.add(item)
        }
    }
}
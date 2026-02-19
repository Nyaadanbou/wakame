package cc.mewcraft.wakame.feature

import cc.mewcraft.lazyconfig.access.entryOrElse
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import com.destroystokyo.paper.loottable.LootableEntityInventory
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.vehicle.VehicleDestroyEvent
import org.bukkit.loot.Lootable


class StopBreakingLootChests : Listener {

    private val stopBlockBreak by FEATURE_CONFIG.entryOrElse(false, "stop_breaking_loot_chests", "block_break")
    private val stopVehicleDestroy by FEATURE_CONFIG.entryOrElse(false, "stop_breaking_loot_chests", "vehicle_destroy")

    @EventHandler(ignoreCancelled = true)
    fun onBreak(event: BlockBreakEvent) {
        val lootable = event.getBlock().state
        if (lootable is Lootable && stopBlockBreak) {
            if (lootable.hasLootTable()) {
                val player: Player = event.player
                if (player.gameMode !== GameMode.CREATIVE) {
                    event.isCancelled = true
                    player.sendMessage(TranslatableMessages.MSG_LOOTCHEST_CANNOT_BE_DESTROYED)
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onDeath(event: VehicleDestroyEvent) {
        val vehicle = event.vehicle
        val attacker = event.attacker
        if (vehicle is LootableEntityInventory && attacker is Player && stopVehicleDestroy) {
            event.isCancelled = true
            attacker.sendMessage(TranslatableMessages.MSG_LOOTCHEST_CANNOT_BE_DESTROYED)
        }
    }
}
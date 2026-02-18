package cc.mewcraft.wakame.feature

import cc.mewcraft.lazyconfig.access.entryOrElse
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import org.bukkit.GameMode
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.loot.Lootable


class StopBreakingLootChests : Listener {

    private val stopBreakingLootChests by FEATURE_CONFIG.entryOrElse(false, "stop_breaking_loot_chests")

    @EventHandler(ignoreCancelled = true)
    fun onBreak(event: BlockBreakEvent) {
        val lootable = event.getBlock().state
        if (lootable is Lootable && stopBreakingLootChests) {
            if (lootable.hasLootTable()) {
                val player: Player = event.player
                if (player.gameMode !== GameMode.CREATIVE) {
                    event.isCancelled = true
                    player.sendMessage(TranslatableMessages.MSG_LOOTCHEST_CANNOT_BE_DESTROYED)
                }
            }
        }
    }

    @EventHandler
    fun onExplode(event: BlockExplodeEvent) {
        val chest = event.getBlock().state
        if (chest is Chest) {
            if (chest.hasLootTable() && stopBreakingLootChests) {
                event.isCancelled = true
            }
        }
    }
}
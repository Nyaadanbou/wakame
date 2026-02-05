package cc.mewcraft.wakame.hook.impl.betonquest.listener

import cc.mewcraft.wakame.integration.dungeon.DungeonBridge
import net.playavalon.mythicdungeons.api.events.dungeon.DungeonDisposeEvent
import net.playavalon.mythicdungeons.api.events.dungeon.DungeonEndEvent
import net.playavalon.mythicdungeons.api.events.dungeon.PlayerFinishDungeonEvent
import net.playavalon.mythicdungeons.api.events.dungeon.PlayerLeaveDungeonEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

object MythicDungeonsListener : Listener {

    //<editor-fold desc="及时让玩家退出队伍">

    @EventHandler
    private fun on(event: DungeonDisposeEvent) {
        for (player in event.instance.players.toList()) {
            DungeonBridge.leaveParty(player.player)
        }
    }

    @EventHandler
    private fun on(event: DungeonEndEvent) {
        for (player in event.gamePlayers.toList()) {
            DungeonBridge.leaveParty(player.player)
        }
    }

    @EventHandler
    private fun on(event: PlayerFinishDungeonEvent) {
        DungeonBridge.leaveParty(event.player)
    }

    @EventHandler
    private fun on(event: PlayerLeaveDungeonEvent) {
        DungeonBridge.leaveParty(event.player)
    }

    @EventHandler
    private fun on(event: PlayerQuitEvent) {
        DungeonBridge.leaveParty(event.player)
    }
    //</editor-fold>
}
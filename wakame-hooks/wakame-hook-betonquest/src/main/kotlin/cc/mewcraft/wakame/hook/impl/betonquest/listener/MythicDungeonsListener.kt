package cc.mewcraft.wakame.hook.impl.betonquest.listener

import cc.mewcraft.wakame.hook.impl.betonquest.util.MythicDungeonsBridge
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
            MythicDungeonsBridge.leaveParty(player.player)
        }
    }

    @EventHandler
    private fun on(event: DungeonEndEvent) {
        for (player in event.gamePlayers.toList()) {
            MythicDungeonsBridge.leaveParty(player.player)
        }
    }

    @EventHandler
    private fun on(event: PlayerFinishDungeonEvent) {
        MythicDungeonsBridge.leaveParty(event.player)
    }

    @EventHandler
    private fun on(event: PlayerLeaveDungeonEvent) {
        MythicDungeonsBridge.leaveParty(event.player)
    }

    @EventHandler
    private fun on(event: PlayerQuitEvent) {
        MythicDungeonsBridge.leaveParty(event.player)
    }
    //</editor-fold>
}
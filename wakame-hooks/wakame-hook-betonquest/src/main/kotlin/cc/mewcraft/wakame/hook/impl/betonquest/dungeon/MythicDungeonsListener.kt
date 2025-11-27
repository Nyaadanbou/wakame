package cc.mewcraft.wakame.hook.impl.betonquest.dungeon

import cc.mewcraft.wakame.hook.impl.betonquest.MythicDungeonsApi
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
        for (player in event.instance.players) {
            MythicDungeonsApi.leaveParty(player.player)
        }
    }

    @EventHandler
    private fun on(event: DungeonEndEvent) {
        for (player in event.gamePlayers) {
            MythicDungeonsApi.leaveParty(player.player)
        }
    }

    @EventHandler
    private fun on(event: PlayerFinishDungeonEvent) {
        MythicDungeonsApi.leaveParty(event.player)
    }

    @EventHandler
    private fun on(event: PlayerLeaveDungeonEvent) {
        MythicDungeonsApi.leaveParty(event.player)
    }

    @EventHandler
    private fun on(event: PlayerQuitEvent) {
        val player = event.player
        MythicDungeonsApi.leaveParty(player)
    }
    //</editor-fold>
}
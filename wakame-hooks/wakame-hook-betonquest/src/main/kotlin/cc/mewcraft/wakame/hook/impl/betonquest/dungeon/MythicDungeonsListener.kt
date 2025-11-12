package cc.mewcraft.wakame.hook.impl.betonquest.dungeon

import net.playavalon.mythicdungeons.MythicDungeons
import net.playavalon.mythicdungeons.api.events.dungeon.DungeonDisposeEvent
import net.playavalon.mythicdungeons.api.events.dungeon.DungeonEndEvent
import net.playavalon.mythicdungeons.api.events.dungeon.PlayerFinishDungeonEvent
import net.playavalon.mythicdungeons.api.events.dungeon.PlayerLeaveDungeonEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

object MythicDungeonsListener : Listener {

    private val mdApi: MythicDungeons
        get() = MythicDungeons.inst()


    //<editor-fold desc="及时让玩家退出队伍">

    @EventHandler
    private fun on(event: DungeonDisposeEvent) {
        for (player in event.instance.players) {
            mdApi.removeFromParty(player.player)
        }
    }

    @EventHandler
    private fun on(event: DungeonEndEvent) {
        for (player in event.gamePlayers) {
            mdApi.removeFromParty(player.player)
        }
    }

    @EventHandler
    private fun on(event: PlayerFinishDungeonEvent) {
        mdApi.removeFromParty(event.player)
    }

    @EventHandler
    private fun on(event: PlayerLeaveDungeonEvent) {
        mdApi.removeFromParty(event.player)
    }

    @EventHandler
    private fun on(event: PlayerQuitEvent) {
        val player = event.player
        mdApi.removeFromParty(player)
    }
    //</editor-fold>
}
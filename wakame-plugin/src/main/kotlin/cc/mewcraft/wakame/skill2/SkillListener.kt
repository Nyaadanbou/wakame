package cc.mewcraft.wakame.skill2

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

internal class SkillListener(
    private val mechanicRecorder: MechanicRecorder,
) : Listener {
    @EventHandler
    private fun on(e: PlayerJoinEvent) {
//        mechanicRecorder.addSkill(e.player)
    }


    @EventHandler
    private fun on(e: PlayerQuitEvent) {
//        mechanicRecorder.removeSkill(e.player)
    }
}
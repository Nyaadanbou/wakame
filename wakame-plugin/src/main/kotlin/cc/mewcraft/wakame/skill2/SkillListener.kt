package cc.mewcraft.wakame.skill2

import cc.mewcraft.wakame.skill2.component.CooldownComponent
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import me.lucko.helper.metadata.Metadata
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import kotlin.jvm.optionals.getOrNull

class SkillListener(
    private val skillManager: SkillManager,
) : Listener {
    @EventHandler
    private fun on(e: ServerTickStartEvent) {
        skillManager.tick()
    }

    @EventHandler
    private fun on(e: PlayerInteractEvent) {
        val metadataMap = Metadata.get(e.player).getOrNull() ?: return
        val cooldownComponent = metadataMap.getOrNull(CooldownComponent.METADATA_KEY)
        cooldownComponent?.cooldown?.timeout = 100f
    }

    @EventHandler
    private fun on(e: PlayerJoinEvent) {
        skillManager.addSkill(e.player)
    }


    @EventHandler
    private fun on(e: PlayerQuitEvent) {
        skillManager.removeSkill(e.player)
    }
}
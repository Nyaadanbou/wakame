package cc.mewcraft.wakame.skill2

import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill2.external.component.Cooldown
import cc.mewcraft.wakame.skill2.system.SkillBukkitEntityMetadataSystem
import cc.mewcraft.wakame.util.Key
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
    private fun on(e: PlayerInteractEvent) {
        val metadataMap = Metadata.get(e.player).getOrNull() ?: return
        val componentMap = metadataMap.getOrNull(SkillBukkitEntityMetadataSystem.COMPONENT_MAP_KEY) ?: return
        val cooldownComponent = componentMap[SkillRegistry.INSTANCES[Key("g22:test")], Cooldown.externalKey]
        cooldownComponent?.cooldown?.timeout = 100f
    }

    @EventHandler
    private fun on(e: PlayerJoinEvent) {
//        skillManager.addSkill(e.player)
    }


    @EventHandler
    private fun on(e: PlayerQuitEvent) {
//        skillManager.removeSkill(e.player)
    }
}
package cc.mewcraft.wakame.compatibility.mythicmobs

import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ISkillMechanic
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent
import io.lumine.mythic.core.skills.SkillExecutor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.io.File

class MythicMobsListener : Listener {
    @EventHandler
    private fun on(e: MythicMechanicLoadEvent) {
        when (e.mechanicName.lowercase()) {
            "nekoattribute" -> e.registerMechanic(::AttributeMechanic)
            "nekoattributemodifier" -> e.registerMechanic(::AttributeModifierMechanic)
        }
    }

    private fun MythicMechanicLoadEvent.registerMechanic(constructor: (SkillExecutor, File, String, MythicLineConfig) -> ISkillMechanic) {
        register(constructor(container.manager, container.file, config.line, config))
    }
}
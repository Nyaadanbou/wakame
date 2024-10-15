package cc.mewcraft.wakame.compatibility

import cc.mewcraft.wakame.compatibility.mechanic.AttributeMechanic
import cc.mewcraft.wakame.compatibility.mechanic.AttributeModifierMechanic
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class MythicMobsCompatibilityListener : Listener {
    @EventHandler
    private fun on(e: MythicMechanicLoadEvent) {
        when (e.mechanicName.lowercase()) {
            "nekoattribute" -> e.register(
                AttributeMechanic(
                    e.container.manager,
                    e.container.file,
                    e.config.line,
                    e.config
                )
            )
            "nekoattributemodifier" -> e.register(
                AttributeModifierMechanic(
                    e.container.manager,
                    e.container.file,
                    e.config.line,
                    e.config
                )
            )
        }
    }
}
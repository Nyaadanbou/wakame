package cc.mewcraft.wakame.compatibility

import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.adapters.AbstractLocation
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ITargetedEntitySkill
import io.lumine.mythic.api.skills.ITargetedLocationSkill
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.SkillResult
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent
import io.lumine.mythic.core.skills.SkillExecutor
import io.lumine.mythic.core.skills.SkillMechanic
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.io.File

class MythicMobsCompatibilityListener : Listener {
    @EventHandler
    private fun on(e: MythicMechanicLoadEvent) {
        when (e.mechanicName.lowercase()) {
            "wakameattribute" -> e.register(
                MythicMobsWakameAttributeMechanic(
                    e.container.manager,
                    e.container.file,
                    e.config.line,
                    e.config
                )
            )
        }
    }
}

private class MythicMobsWakameAttributeMechanic(
    manager: SkillExecutor,
    file: File,
    line: String,
    mlc: MythicLineConfig,
) : SkillMechanic(manager, file, line, mlc), ITargetedEntitySkill, ITargetedLocationSkill {
    override fun castAtLocation(metadata: SkillMetadata, target: AbstractLocation): SkillResult {
        TODO("Not yet implemented")
    }

    override fun castAtEntity(metadata: SkillMetadata, target: AbstractEntity): SkillResult {
        TODO("Not yet implemented")
    }
}
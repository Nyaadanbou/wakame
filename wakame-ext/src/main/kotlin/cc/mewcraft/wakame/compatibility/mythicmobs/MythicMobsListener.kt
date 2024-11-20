package cc.mewcraft.wakame.compatibility.mythicmobs

import cc.mewcraft.wakame.compatibility.mythicmobs.condition.*
import cc.mewcraft.wakame.compatibility.mythicmobs.drop.NekoItemDrop
import cc.mewcraft.wakame.compatibility.mythicmobs.mechanic.*
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.drops.IDrop
import io.lumine.mythic.api.skills.ISkillMechanic
import io.lumine.mythic.api.skills.conditions.ISkillCondition
import io.lumine.mythic.bukkit.events.*
import io.lumine.mythic.core.skills.SkillExecutor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.io.File

class MythicMobsListener : Listener {

    @EventHandler
    private fun on(e: MythicConditionLoadEvent) {
        when (e.conditionName.lowercase()) {
            "nekohasitem" -> e.registerCondition(::HasItemCondition)
            "nekoholding" -> e.registerCondition(::HoldingCondition)
            "nekolevel" -> e.registerCondition(::LevelCondition)
        }
    }

    @EventHandler
    private fun on(e: MythicMechanicLoadEvent) {
        when (e.mechanicName.lowercase()) {
            "nekoattribute" -> e.registerMechanic(::AttributeMechanic)
            "nekoattributemodifier" -> e.registerMechanic(::AttributeModifierMechanic)
            "nekodamage", "nekobasedamage" -> e.registerMechanic(::NekoBaseDamageMechanic)
            "nekopercentdamage" -> e.registerMechanic(::NekoPercentDamageMechanic)
            "nekoremoveattributemodifier" -> e.registerMechanic(::RemoveAttributeModifierMechanic)
        }
    }

    @EventHandler
    private fun on(e: MythicDropLoadEvent) {
        when (e.dropName.lowercase()) {
            "nekodrop" -> e.registerDrop(::NekoItemDrop)
        }
    }

    private fun MythicConditionLoadEvent.registerCondition(constructor: (String, MythicLineConfig) -> ISkillCondition) {
        register(constructor(config.line, config))
    }

    private fun MythicMechanicLoadEvent.registerMechanic(constructor: (SkillExecutor, File, String, MythicLineConfig) -> ISkillMechanic) {
        register(constructor(container.manager, container.file, config.line, config))
    }

    private fun MythicDropLoadEvent.registerDrop(constructor: (MythicLineConfig, String) -> IDrop) {
        register(constructor(config, config.line))
    }
}
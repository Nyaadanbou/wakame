package cc.mewcraft.wakame.compatibility.mythicmobs

import cc.mewcraft.wakame.compatibility.mythicmobs.condition.HasItemCondition
import cc.mewcraft.wakame.compatibility.mythicmobs.condition.HoldingCondition
import cc.mewcraft.wakame.compatibility.mythicmobs.drop.NekoDrop
import cc.mewcraft.wakame.compatibility.mythicmobs.mechanic.AttributeMechanic
import cc.mewcraft.wakame.compatibility.mythicmobs.mechanic.AttributeModifierMechanic
import cc.mewcraft.wakame.compatibility.mythicmobs.mechanic.RemoveAttributeModifierMechanic
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.drops.IDrop
import io.lumine.mythic.api.skills.ISkillMechanic
import io.lumine.mythic.api.skills.conditions.ISkillCondition
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent
import io.lumine.mythic.bukkit.events.MythicDropLoadEvent
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent
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
        }
    }

    @EventHandler
    private fun on(e: MythicMechanicLoadEvent) {
        when (e.mechanicName.lowercase()) {
            "nekoattribute" -> e.registerMechanic(::AttributeMechanic)
            "nekoattributemodifier" -> e.registerMechanic(::AttributeModifierMechanic)
            "nekoremoveattributemodifier" -> e.registerMechanic(::RemoveAttributeModifierMechanic)
        }
    }

    @EventHandler
    private fun on(e: MythicDropLoadEvent) {
        when (e.dropName.lowercase()) {
            "nekodrop" -> e.registerDrop(::NekoDrop)
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
package cc.mewcraft.wakame.hook.impl.mythicmobs.listener

import cc.mewcraft.wakame.hook.impl.mythicmobs.condition.*
import cc.mewcraft.wakame.hook.impl.mythicmobs.drop.NekoItemDrop
import cc.mewcraft.wakame.hook.impl.mythicmobs.mechanic.*
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

object ConfigListener : Listener {

    @EventHandler
    fun on(e: MythicConditionLoadEvent) {
        when (e.conditionName.lowercase()) {
            "nekohasitem" -> {
                e.registerCondition(::HasItemCondition)
            }

            "nekoholding" -> {
                e.registerCondition(::HoldingCondition)
            }

            "nekolevel" -> {
                e.registerCondition(::LevelCondition)
            }

            "koishmana", "mana" -> {
                e.registerCondition(::ManaCondition)
            }

            "mainhanditemgrouponcooldown" -> {
                e.registerCondition(::MainhandItemGroupOnCooldown)
            }
        }
    }

    @EventHandler
    fun on(e: MythicMechanicLoadEvent) {
        when (e.mechanicName.lowercase()) {
            "nekoattribute" -> {
                e.registerMechanic(::AttributeMechanic)
            }

            "nekoattributemodifier" -> {
                e.registerMechanic(::AttributeModifierMechanic)
            }

            "nekodamage", "nekobasedamage" -> {
                e.registerMechanic(::NekoBaseDamageMechanic)
            }

            "nekopercentdamage" -> {
                e.registerMechanic(::NekoPercentDamageMechanic)
            }

            "nekoremoveattributemodifier" -> {
                e.registerMechanic(::RemoveAttributeModifierMechanic)
            }

            "koishrestoremana", "restoremana" -> {
                e.registerMechanic(::RestoreManaMechanic)
            }

            "koishrestoremanapercent", "restoremanapercent" -> {
                e.registerMechanic(::RestoreManaPercentMechanic)
            }

            "koishconsumemana", "consumemana" -> {
                e.registerMechanic(::ConsumeManaMechanic)
            }

            "koishconsumemanapercent", "consumemanapercent" -> {
                e.registerMechanic(::ConsumeManaPercentMechanic)
            }
        }
    }

    @EventHandler
    fun on(e: MythicDropLoadEvent) {
        when (e.dropName.lowercase()) {
            "nekodrop" -> {
                e.registerDrop(::NekoItemDrop)
            }
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
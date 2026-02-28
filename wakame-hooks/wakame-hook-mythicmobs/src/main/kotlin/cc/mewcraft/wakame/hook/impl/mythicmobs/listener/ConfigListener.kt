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
    fun on(event: MythicConditionLoadEvent) = with(event) {
        when (conditionName.lowercase()) {
            "koish:has_item", "nekohasitem" -> registerCondition(::HasItemCondition)
            "koish:holding", "nekoholding" -> registerCondition(::HoldingCondition)
            "koish:inscription", "inscription" -> registerCondition(::InscriptionCondition)
            "koish:level", "nekolevel" -> registerCondition(::LevelCondition)
            "koish:mana", "mana" -> registerCondition(::ManaCondition)
            "koish:mainhand_item_group_on_cooldown", "mainhanditemgrouponcooldown" -> registerCondition(::MainhandItemGroupOnCooldown)
        }
    }

    @EventHandler
    fun on(event: MythicMechanicLoadEvent) = with(event) {
        when (mechanicName.lowercase()) {
            "koish:attribute", "nekoattribute" -> registerMechanic(::AttributeMechanic)
            "koish:attribute_modifier", "nekoattributemodifier" -> registerMechanic(::AttributeModifierMechanic)
            "koish:damage", "nekodamage", "nekobasedamage" -> registerMechanic(::DamageMechanic)
            "koish:damage_percent", "nekopercentdamage" -> registerMechanic(::DamagePercentMechanic)
            "koish:remove_attribute_modifier", "nekoremoveattributemodifier" -> registerMechanic(::RemoveAttributeModifierMechanic)
            "koish:restore_mana", "koishrestoremana", "restoremana" -> registerMechanic(::RestoreManaMechanic)
            "koish:restore_mana_percent", "koishrestoremanapercent", "restoremanapercent" -> registerMechanic(::RestoreManaPercentMechanic)
            "koish:consume_mana", "koishconsumemana", "consumemana" -> registerMechanic(::ConsumeManaMechanic)
            "koish:consume_mana_percent", "koishconsumemanapercent", "consumemanapercent" -> registerMechanic(::ConsumeManaPercentMechanic)
        }
    }

    @EventHandler
    fun on(event: MythicDropLoadEvent) = with(event) {
        when (dropName.lowercase()) {
            "koish:item", "nekodrop" -> registerDrop(::NekoItemDrop)
        }
    }

    context(event: MythicConditionLoadEvent)
    private fun registerCondition(constructor: (String, MythicLineConfig) -> ISkillCondition) {
        event.register(constructor(event.config.line, event.config))
    }

    context(event: MythicMechanicLoadEvent)
    private fun registerMechanic(constructor: (SkillExecutor, File, String, MythicLineConfig) -> ISkillMechanic) {
        event.register(constructor(event.container.manager, event.container.file, event.config.line, event.config))
    }

    context(event: MythicDropLoadEvent)
    private fun registerDrop(constructor: (MythicLineConfig, String) -> IDrop) {
        event.register(constructor(event.config, event.config.line))
    }
}
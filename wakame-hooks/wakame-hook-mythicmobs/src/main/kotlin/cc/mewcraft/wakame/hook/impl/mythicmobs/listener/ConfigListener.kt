package cc.mewcraft.wakame.hook.impl.mythicmobs.listener

import cc.mewcraft.wakame.hook.impl.mythicmobs.condition.*
import cc.mewcraft.wakame.hook.impl.mythicmobs.drop.KoishItemDrop
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
            "koish_has_item", "nekohasitem" -> registerCondition(::HasItemCondition)
            "koish_holding", "nekoholding" -> registerCondition(::HoldingCondition)
            "koish_inscription", "inscription" -> registerCondition(::InscriptionCondition)
            "koish_level", "nekolevel" -> registerCondition(::LevelCondition)
            "koish_mana", "mana" -> registerCondition(::ManaCondition)
            "koish_item_group_on_cooldown", "mainhanditemgrouponcooldown" -> registerCondition(::ItemGroupOnCooldownCondition)
        }
    }

    @EventHandler
    fun on(event: MythicMechanicLoadEvent) = with(event) {
        when (mechanicName.lowercase()) {
            "koish_attribute", "nekoattribute" -> registerMechanic(::AttributeMechanic)
            "koish_attribute_modifier", "nekoattributemodifier" -> registerMechanic(::AttributeModifierMechanic)
            "koish_consume_mana", "koishconsumemana", "consumemana" -> registerMechanic(::ConsumeManaMechanic)
            "koish_consume_mana_percent", "koishconsumemanapercent", "consumemanapercent" -> registerMechanic(::ConsumeManaPercentMechanic)
            "koish_damage", "nekodamage", "nekobasedamage" -> registerMechanic(::DamageMechanic)
            "koish_damage_percent", "nekopercentdamage" -> registerMechanic(::DamagePercentMechanic)
            "koish_damage_attribute_map", "nekopaneldamage" -> registerMechanic(::DamageAttributeMapMechanic)
            "koish_remove_attribute_modifier", "nekoremoveattributemodifier" -> registerMechanic(::RemoveAttributeModifierMechanic)
            "koish_reset_item_group_cooldown" -> registerMechanic(::ResetItemGroupCooldownMechanic)
            "koish_restore_mana", "koishrestoremana", "restoremana" -> registerMechanic(::RestoreManaMechanic)
            "koish_restore_mana_percent", "koishrestoremanapercent", "restoremanapercent" -> registerMechanic(::RestoreManaPercentMechanic)
        }
    }

    @EventHandler
    fun on(event: MythicDropLoadEvent) = with(event) {
        when (dropName.lowercase()) {
            "koish_item", "nekodrop" -> registerDrop(::KoishItemDrop)
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
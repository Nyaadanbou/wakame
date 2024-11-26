package cc.mewcraft.wakame.hook.impl.mythicmobs

import cc.mewcraft.wakame.damage.DamageApplier
import cc.mewcraft.wakame.hook.impl.mythicmobs.condition.HasItemCondition
import cc.mewcraft.wakame.hook.impl.mythicmobs.condition.HoldingCondition
import cc.mewcraft.wakame.hook.impl.mythicmobs.condition.LevelCondition
import cc.mewcraft.wakame.hook.impl.mythicmobs.drop.NekoItemDrop
import cc.mewcraft.wakame.hook.impl.mythicmobs.mechanic.AttributeMechanic
import cc.mewcraft.wakame.hook.impl.mythicmobs.mechanic.AttributeModifierMechanic
import cc.mewcraft.wakame.hook.impl.mythicmobs.mechanic.NekoBaseDamageMechanic
import cc.mewcraft.wakame.hook.impl.mythicmobs.mechanic.NekoPercentDamageMechanic
import cc.mewcraft.wakame.hook.impl.mythicmobs.mechanic.RemoveAttributeModifierMechanic
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.protection.ProtectionManager
import cc.mewcraft.wakame.util.registerEvents
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.drops.IDrop
import io.lumine.mythic.api.skills.ISkillMechanic
import io.lumine.mythic.api.skills.conditions.ISkillCondition
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent
import io.lumine.mythic.bukkit.events.MythicDamageEvent
import io.lumine.mythic.bukkit.events.MythicDropLoadEvent
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent
import io.lumine.mythic.core.skills.SkillExecutor
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.io.File

@Hook(plugins = ["MythicMobs"])
object MythicMobsHook : Listener {
    init {
        // 注册 Listeners
        registerEvents()

        // 注册 DamageApplier
        // 这应该覆盖掉默认的实例
        DamageApplier.register(MythicMobsDamageApplier)
    }

    // SkillAdapter#doDamage 会触发该事件, 我们必须在这个事件里修改结果使其遵循领地和保护区的规则.
    // Bukkit 的 EntityDamageEvent 也会被触发, 但那个事件里的 DamageSource 完全没法正常使用.
    // 原因见: MythicMobsDamageApplier.kt
    @EventHandler
    fun on(e: MythicDamageEvent) {
        val damager = e.caster.entity.bukkitEntity as? Player ?: return
        val victim = e.target.bukkitEntity as? LivingEntity ?: return

        e.isCancelled = !ProtectionManager.canHurtEntity(damager, victim, null)
    }

    @EventHandler
    fun on(e: MythicConditionLoadEvent) {
        when (e.conditionName.lowercase()) {
            "nekohasitem" -> e.registerCondition(::HasItemCondition)
            "nekoholding" -> e.registerCondition(::HoldingCondition)
            "nekolevel" -> e.registerCondition(::LevelCondition)
        }
    }

    @EventHandler
    fun on(e: MythicMechanicLoadEvent) {
        when (e.mechanicName.lowercase()) {
            "nekoattribute" -> e.registerMechanic(::AttributeMechanic)
            "nekoattributemodifier" -> e.registerMechanic(::AttributeModifierMechanic)
            "nekodamage", "nekobasedamage" -> e.registerMechanic(::NekoBaseDamageMechanic)
            "nekopercentdamage" -> e.registerMechanic(::NekoPercentDamageMechanic)
            "nekoremoveattributemodifier" -> e.registerMechanic(::RemoveAttributeModifierMechanic)
        }
    }

    @EventHandler
    fun on(e: MythicDropLoadEvent) {
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
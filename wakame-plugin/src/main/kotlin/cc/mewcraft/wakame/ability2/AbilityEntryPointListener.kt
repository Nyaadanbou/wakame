package cc.mewcraft.wakame.ability2

import cc.mewcraft.wakame.ability2.trigger.AbilitySingleTrigger
import cc.mewcraft.wakame.entity.player.combo
import cc.mewcraft.wakame.event.bukkit.PlayerItemLeftClickEvent
import cc.mewcraft.wakame.event.bukkit.PlayerItemRightClickEvent
import cc.mewcraft.wakame.event.bukkit.PlayerManaConsumeEvent
import cc.mewcraft.wakame.event.bukkit.PlayerNotEnoughManaEvent
import cc.mewcraft.wakame.item2.config.property.ItemPropertyTypes
import cc.mewcraft.wakame.item2.getProperty
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.item.takeUnlessEmpty
import cc.mewcraft.wakame.util.registerEvents
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileHitEvent

@Init(stage = InitStage.POST_WORLD)
internal object AbilityEntryPointListener : Listener {

    @InitFun
    fun init() {
        registerEvents()
    }

    // ------------
    // Ability Entry Point
    // ------------

    @EventHandler
    fun onLeftClickItem(event: PlayerItemLeftClickEvent) {
        val player = event.player
        player.combo.handleTrigger(AbilitySingleTrigger.LEFT_CLICK)
    }

    @EventHandler
    fun onRightClickItem(event: PlayerItemRightClickEvent) {
        val player = event.player
        player.combo.handleTrigger(AbilitySingleTrigger.RIGHT_CLICK)
    }

    @EventHandler
    fun onManaCost(event: PlayerManaConsumeEvent) {
        val player = event.player
        AbilityDisplay.displayManaCost(event.amount, player, event.ability.display)
    }

    @EventHandler
    fun onNotEnoughMana(event: PlayerNotEnoughManaEvent) {
        val player = event.player
        AbilityDisplay.displayNotEnoughMana(player, event.ability.display)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onProjectileHit(event: ProjectileHitEvent) {
        val projectile = event.entity
        val hitEntity = event.hitEntity
        when (projectile) {
            is AbstractArrow -> {
                val itemStack = projectile.itemStack.takeUnlessEmpty() ?: return
                val abilityOnItem = itemStack.getProperty(ItemPropertyTypes.ABILITY) ?: return
                val caster = projectile.shooter as? LivingEntity ?: return
                val target = hitEntity ?: return
                AbilityCastUtils.castPoint(abilityOnItem.meta.unwrap(), caster, target)
            }
        }
    }
}
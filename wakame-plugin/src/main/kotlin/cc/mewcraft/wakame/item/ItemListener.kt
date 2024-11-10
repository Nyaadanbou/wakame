@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.event.NekoEntityDamageEvent
import cc.mewcraft.wakame.event.PlayerItemSlotChangeEvent
import cc.mewcraft.wakame.event.PlayerSkillPrepareCastEvent
import cc.mewcraft.wakame.item.logic.ItemSlotChangeRegistry
import cc.mewcraft.wakame.player.equipment.ArmorChangeEvent
import cc.mewcraft.wakame.player.interact.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.skill.SkillEventHandler
import cc.mewcraft.wakame.util.takeUnlessEmpty
import io.papermc.paper.event.player.PlayerStopUsingItemEvent
import org.bukkit.Bukkit
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.ThrowableProjectile
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 监听玩家物品栏中的物品所发生的变化.
 * 这些事件都有 “新/旧物品” 两个变量.
 */
internal class ItemChangeListener : KoinComponent, Listener {
    @EventHandler
    fun on(event: PlayerItemSlotChangeEvent) {
        ItemSlotChangeRegistry.listeners().forEach { listener -> listener.handleEvent(event) }
    }
}

/**
 * 监听物品与世界发生的交互事件.
 * 这些都是 *物品行为* 的一部分.
 */
internal class ItemBehaviorListener : KoinComponent, Listener {
    @EventHandler
    fun on(event: ArmorChangeEvent) {
        val player = event.player
        val previous = event.previous?.takeUnlessEmpty()
        val current = event.current?.takeUnlessEmpty()

        previous?.shadowNeko(false)?.behaviors?.forEach { behavior ->
            behavior.handleEquip(player, previous, false, event)
        }
        current?.shadowNeko(false)?.behaviors?.forEach { behavior ->
            behavior.handleEquip(player, current, true, event)
        }
    }

    @EventHandler
    fun on(wrappedEvent: WrappedPlayerInteractEvent) {
        val event = wrappedEvent.event
        val item = event.item ?: return
        val nekoStack = item.shadowNeko(false) ?: return
        nekoStack.behaviors.forEach { behavior ->
            if (event.useItemInHand() == Event.Result.DENY) {
                return
            }
            behavior.handleInteract(event.player, item, event.action, wrappedEvent)
        }
    }

    @EventHandler
    fun on(event: PlayerInteractAtEntityEvent) {
        val item = event.player.inventory.itemInMainHand.takeUnlessEmpty()
        val nekoStack = item?.shadowNeko(false) ?: return
        nekoStack.behaviors.forEach { behavior ->
            if (event.isCancelled) {
                return
            }
            behavior.handleInteractAtEntity(event.player, item, event.rightClicked, event)
        }
    }

    @EventHandler
    fun on(event: NekoEntityDamageEvent) {
        val player = event.damageSource.causingEntity as? Player ?: return
        val item = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return
        val nekoStack = item.shadowNeko(false) ?: return
        nekoStack.behaviors.forEach { behavior ->
            if (event.isCancelled) {
                return
            }
            behavior.handleAttackEntity(player, item, event.damagee, event)
        }
    }

    @EventHandler
    fun on(event: ProjectileLaunchEvent) {
        val projectile = event.entity
        val item = when (projectile) {
            is AbstractArrow -> {
                projectile.itemStack
            }

            is ThrowableProjectile -> {
                projectile.item
            }

            else -> {
                return
            }
        }
        val ownerUniqueId = projectile.ownerUniqueId ?: return
        val player = Bukkit.getPlayer(ownerUniqueId) ?: return
        val nekoStack = item.shadowNeko(false) ?: return
        nekoStack.behaviors.forEach { behavior ->
            if (event.isCancelled) {
                return
            }
            behavior.handleItemProjectileLaunch(player, item, projectile, event)
        }
    }

    @EventHandler
    fun on(event: ProjectileHitEvent) {
        val projectile = event.entity
        val item = when (projectile) {
            is AbstractArrow -> {
                projectile.itemStack
            }

            is ThrowableProjectile -> {
                projectile.item
            }

            else -> {
                return
            }
        }
        val ownerUniqueId = projectile.ownerUniqueId ?: return
        val player = Bukkit.getPlayer(ownerUniqueId) ?: return
        val nekoStack = item.shadowNeko(false) ?: return
        nekoStack.behaviors.forEach { behavior ->
            if (event.isCancelled) {
                return
            }
            behavior.handleItemProjectileHit(player, item, projectile, event)
        }
    }

    @EventHandler
    fun on(event: BlockBreakEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return
        val nekoStack = item.shadowNeko(false) ?: return
        nekoStack.behaviors.forEach { behavior ->
            if (event.isCancelled) {
                return
            }
            behavior.handleBreakBlock(player, item, event)
        }
    }

    @EventHandler
    fun on(event: PlayerItemDamageEvent) {
        val item = event.item.takeUnlessEmpty() ?: return
        val nekoStack = item.shadowNeko(false) ?: return
        nekoStack.behaviors.forEach { behavior ->
            if (event.isCancelled) {
                return
            }
            behavior.handleDamage(event.player, item, event)
        }
    }

    @EventHandler
    fun on(event: PlayerItemBreakEvent) {
        val item = event.brokenItem.takeUnlessEmpty() ?: return
        val nekoStack = item.shadowNeko(false) ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleBreak(event.player, item, event)
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        val clickedItem = event.currentItem
        val cursorItem = event.cursor.takeUnlessEmpty()

        clickedItem?.shadowNeko(false)?.behaviors?.forEach { behavior ->
            behavior.handleInventoryClick(player, clickedItem, event)
        }
        cursorItem?.shadowNeko(false)?.behaviors?.forEach { behavior ->
            behavior.handleInventoryClickOnCursor(player, cursorItem, event)
        }

        if (event.click == ClickType.NUMBER_KEY) {
            val hotbarItem = player.inventory.getItem(event.hotbarButton)
            hotbarItem?.shadowNeko(false)?.behaviors?.forEach { behavior ->
                behavior.handleInventoryHotbarSwap(player, hotbarItem, event)
            }
        }
    }

    @EventHandler
    fun on(event: PlayerStopUsingItemEvent) {
        val item = event.item.takeUnlessEmpty() ?: return
        val nekoStack = item.shadowNeko(false) ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleRelease(event.player, item, event)
        }
    }

    @EventHandler
    fun on(event: PlayerItemConsumeEvent) {
        val item = event.item.takeUnlessEmpty() ?: return
        val nekoStack = item.shadowNeko(false) ?: return
        nekoStack.behaviors.forEach { behavior ->
            if (event.isCancelled) {
                return
            }
            behavior.handleConsume(event.player, item, event)
        }
    }

    @EventHandler
    fun on(event: PlayerSkillPrepareCastEvent) {
        val item = event.item ?: return
        val nekoStack = item.shadowNeko(false) ?: return
        nekoStack.behaviors.forEach { behavior ->
            if (event.isCancelled) {
                return
            }
            behavior.handleSkillPrepareCast(event.caster, item, event.skill, event)
        }
    }
}

/**
 * 无法被分类到 [ItemChangeListener] 和 [ItemBehaviorListener] 的监听逻辑.
 * 最终这些都应该合并到 [ItemChangeListener] 或 [ItemBehaviorListener] 中去.
 */
// FIXME 合并到 [ItemChangeListener] 或 [ItemBehaviorListener] 中去
internal class ItemMiscellaneousListener : KoinComponent, Listener {
    private val skillEventHandler: SkillEventHandler by inject()

    //<editor-fold desc="Skills">
    @EventHandler
    fun on(event: PlayerInteractEvent) {
        val slot = event.hand ?: return
        val player = event.player
        val item = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return
        val nekoStack = item.shadowNeko(false) ?: return
        if (!nekoStack.slotGroup.test(slot))
            return

        when (event.action) {
            Action.LEFT_CLICK_BLOCK -> {
                skillEventHandler.onLeftClickBlock(player, item, event.clickedBlock?.location!!, event)
            }

            Action.LEFT_CLICK_AIR -> {
                skillEventHandler.onLeftClickAir(player, item, event)
            }

            Action.RIGHT_CLICK_BLOCK -> {
                skillEventHandler.onRightClickBlock(player, item, event.clickedBlock?.location!!, event)
            }

            Action.RIGHT_CLICK_AIR -> {
                skillEventHandler.onRightClickAir(player, item, event)
            }

            else -> return
        }
    }

    @EventHandler
    fun on2(event: EntityDamageByEntityEvent) {
        val damager = event.damager as? Player ?: return
        val entity = event.entity as? LivingEntity ?: return
        val item = damager.inventory.itemInMainHand.takeUnlessEmpty() ?: return
        skillEventHandler.onAttack(damager, entity, item, event)
    }

    @EventHandler
    fun on(event: ProjectileHitEvent) {
        skillEventHandler.onProjectileHit(event.entity, event.hitEntity)
    }
    //</editor-fold>
}
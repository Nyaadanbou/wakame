@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.event.NekoEntityDamageEvent
import cc.mewcraft.wakame.event.PlayerItemSlotChangeEvent
import cc.mewcraft.wakame.event.PlayerSkillPrepareCastEvent
import cc.mewcraft.wakame.integration.protection.ProtectionManager
import cc.mewcraft.wakame.item.logic.ItemSlotChangeRegistry
import cc.mewcraft.wakame.player.equipment.ArmorChangeEvent
import cc.mewcraft.wakame.player.interact.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.skill.SkillEventHandler
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.takeUnlessEmpty
import io.papermc.paper.event.player.PlayerStopUsingItemEvent
import org.bukkit.Bukkit
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.ThrowableProjectile
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
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 监听玩家物品栏中的物品所发生的变化.
 * 这些事件都有 “新/旧物品” 两个变量.
 */
internal class ItemChangeListener : KoinComponent, Listener {
    @EventHandler
    fun on(event: PlayerItemSlotChangeEvent) {
        for (listener in ItemSlotChangeRegistry.listeners) {
            try {
                listener.handleEvent(event)
            } catch (_: Exception) {
                LOGGER.error(
                    """
                    An error occurred while executing listener: ${listener::class.simpleName} 
                    
                    Player: ${event.player.name}
                    Slot: ${event.slot}
                    Prev: ${event.oldItemStack}
                    Curr: ${event.newItemStack}
                    """.trimIndent()
                )
            }
        }
    }
}

/**
 * 萌芽是否应该处理该玩家?
 *
 * TODO 文档待补充.
 */
private fun isHandleable(player: Player): Boolean {
    return player.toUser().isInventoryListenable
}

/**
 * 监听物品与世界发生的交互事件.
 * 这些都是 *物品行为* 的一部分.
 */
internal class ItemBehaviorListener : KoinComponent, Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: ArmorChangeEvent) {
        val player = event.player

        if (!isHandleable(player)) {
            return
        }

        val previous = event.previous?.takeUnlessEmpty()
        val current = event.current?.takeUnlessEmpty()

        previous?.shadowNeko()?.handleEquip(player, previous, false, event)
        current?.shadowNeko()?.handleEquip(player, current, true, event)
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun on(wrappedEvent: WrappedPlayerInteractEvent) {
        val event = wrappedEvent.event
        val player = event.player

        if (!isHandleable(player)) {
            return
        }

        val itemStack = event.item ?: return
        val nekoStack = itemStack.shadowNeko() ?: return

        val location = event.clickedBlock?.location ?: player.location
        if (!ProtectionManager.canUseItem(player, itemStack, location)) {
            return
        }

        // 该事件比较特殊, 无论是否被“取消”, 总是传递给物品行为

        nekoStack.handleInteract(event.player, itemStack, event.action, wrappedEvent)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: PlayerInteractAtEntityEvent) {
        val player = event.player

        if (!isHandleable(player)) {
            return
        }

        val itemStack = event.player.inventory.itemInMainHand.takeUnlessEmpty()
        val nekoStack = itemStack?.shadowNeko() ?: return

        nekoStack.handleInteractAtEntity(event.player, itemStack, event.rightClicked, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: NekoEntityDamageEvent) {
        val player = event.damageSource.causingEntity as? Player ?: return

        if (!isHandleable(player)) {
            return
        }

        val itemStack = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return
        val nekoStack = itemStack.shadowNeko() ?: return
        nekoStack.handleAttackEntity(player, itemStack, event.damagee, event)
    }

    private fun getItemFromProjectile(projectile: Projectile): ItemStack? {
        return when (projectile) {
            is AbstractArrow -> {
                projectile.itemStack
            }

            is ThrowableProjectile -> {
                projectile.item
            }

            else -> {
                null
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: ProjectileLaunchEvent) {
        val projectile = event.entity
        val itemStack = getItemFromProjectile(projectile) ?: return
        val ownerUniqueId = projectile.ownerUniqueId ?: return
        val player = Bukkit.getPlayer(ownerUniqueId) ?: return

        if (!isHandleable(player)) {
            return
        }

        itemStack.shadowNeko()?.handleItemProjectileLaunch(player, itemStack, projectile, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: ProjectileHitEvent) {
        val projectile = event.entity
        val itemStack = getItemFromProjectile(projectile) ?: return
        val ownerUniqueId = projectile.ownerUniqueId ?: return
        val player = Bukkit.getPlayer(ownerUniqueId) ?: return

        if (!isHandleable(player)) {
            return
        }

        itemStack.shadowNeko()?.handleItemProjectileHit(player, itemStack, projectile, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: BlockBreakEvent) {
        val player = event.player

        if (!isHandleable(player)) {
            return
        }

        val itemStack = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return
        val nekoStack = itemStack.shadowNeko() ?: return
        nekoStack.handleBreakBlock(player, itemStack, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: PlayerItemDamageEvent) {
        val player = event.player

        if (!isHandleable(player)) {
            return
        }

        val itemStack = event.item.takeUnlessEmpty() ?: return
        val nekoStack = itemStack.shadowNeko() ?: return
        nekoStack.handleDamage(player, itemStack, event)
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun on(event: PlayerItemBreakEvent) {
        val player = event.player

        if (!isHandleable(player)) {
            return
        }

        val itemStack = event.brokenItem.takeUnlessEmpty() ?: return
        val nekoStack = itemStack.shadowNeko() ?: return
        nekoStack.handleBreak(player, itemStack, event)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: InventoryClickEvent) {
        val player = event.whoClicked as Player

        if (!isHandleable(player)) {
            return
        }

        val clickedItem = event.currentItem
        val cursorItem = event.cursor.takeUnlessEmpty()

        clickedItem?.shadowNeko()?.handleInventoryClick(player, clickedItem, event)
        cursorItem?.shadowNeko()?.handleInventoryClickOnCursor(player, cursorItem, event)

        if (event.click == ClickType.NUMBER_KEY) {
            val hotbarItem = player.inventory.getItem(event.hotbarButton)
            hotbarItem?.shadowNeko()?.handleInventoryHotbarSwap(player, hotbarItem, event)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun on(event: PlayerStopUsingItemEvent) {
        val player = event.player

        if (!isHandleable(player)) {
            return
        }

        val itemStack = event.item.takeUnlessEmpty() ?: return
        val nekoStack = itemStack.shadowNeko() ?: return
        nekoStack.handleRelease(player, itemStack, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: PlayerItemConsumeEvent) {
        val player = event.player

        if (!isHandleable(player)) {
            return
        }

        val itemStack = event.item.takeUnlessEmpty() ?: return
        val nekoStack = itemStack.shadowNeko() ?: return
        nekoStack.handleConsume(player, itemStack, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: PlayerSkillPrepareCastEvent) {
        val player = event.caster

        if (!isHandleable(player)) {
            return
        }

        val itemStack = event.item ?: return
        val nekoStack = itemStack.shadowNeko() ?: return
        nekoStack.handleSkillPrepareCast(player, itemStack, event.skill, event)
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

        if (!isHandleable(player)) {
            return
        }

        val itemStack = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return
        val nekoStack = itemStack.shadowNeko() ?: return
        if (!nekoStack.slotGroup.test(slot))
            return

        when (event.action) {
            Action.LEFT_CLICK_BLOCK -> {
                skillEventHandler.onLeftClickBlock(player, itemStack, event.clickedBlock?.location!!, event)
            }

            Action.LEFT_CLICK_AIR -> {
                skillEventHandler.onLeftClickAir(player, itemStack, event)
            }

            Action.RIGHT_CLICK_BLOCK -> {
                skillEventHandler.onRightClickBlock(player, itemStack, event.clickedBlock?.location!!, event)
            }

            Action.RIGHT_CLICK_AIR -> {
                skillEventHandler.onRightClickAir(player, itemStack, event)
            }

            else -> return
        }
    }

    @EventHandler
    fun on2(event: EntityDamageByEntityEvent) {
        val damager = event.damager as? Player ?: return

        if (!isHandleable(damager)) {
            return
        }

        val entity = event.entity as? LivingEntity ?: return
        val itemStack = damager.inventory.itemInMainHand.takeUnlessEmpty() ?: return
        skillEventHandler.onAttack(damager, entity, itemStack, event)
    }

    @EventHandler
    fun on(event: ProjectileHitEvent) {
        skillEventHandler.onProjectileHit(event.entity, event.hitEntity)
    }
    //</editor-fold>
}
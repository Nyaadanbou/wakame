package cc.mewcraft.wakame.item2

import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.entity.player.isInventoryListenable
import cc.mewcraft.wakame.event.bukkit.PlayerItemLeftClickEvent
import cc.mewcraft.wakame.event.bukkit.PlayerItemRightClickEvent
import cc.mewcraft.wakame.event.bukkit.PostprocessDamageEvent
import cc.mewcraft.wakame.event.bukkit.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.integration.protection.ProtectionManager
import cc.mewcraft.wakame.item2.config.property.impl.ItemSlotRegistry
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.item.takeUnlessEmpty
import cc.mewcraft.wakame.util.registerEvents
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent
import io.papermc.paper.event.player.PlayerStopUsingItemEvent
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.ThrowableProjectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.ItemStack

@Init(stage = InitStage.POST_WORLD)
internal object ItemBehaviorListener : Listener {

    @InitFun
    fun init() {
        registerEvents()
    }

    // ------------
    // Item Behavior
    // ------------

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: EntityEquipmentChangedEvent) {
        val player = event.entity as? Player ?: return
        if (!player.isInventoryListenable) return
        for ((slot, change) in event.equipmentChanges) {
            val previous = change.oldItem().takeUnlessEmpty()
            val current = change.newItem().takeUnlessEmpty()
            if (previous == current)
                continue

            change.oldItem().takeUnlessEmpty()?.handleEquip(player, change.oldItem(), slot, false, event)
            change.newItem().takeUnlessEmpty()?.handleEquip(player, change.newItem(), slot, true, event)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun on(event: PlayerItemLeftClickEvent) {
        val player = event.player
        if (!player.isInventoryListenable) return
        val itemstack = event.item

        itemstack.handleLeftClick(player, itemstack, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun on(event: PlayerItemRightClickEvent) {
        val player = event.player
        if (!player.isInventoryListenable) return
        val itemstack = event.item

        itemstack.handleRightClick(player, itemstack, event.hand, event)
    }

    @EventHandler(priority = EventPriority.NORMAL)
    fun on(wrappedEvent: WrappedPlayerInteractEvent) {
        val event = wrappedEvent.event
        val player = event.player
        if (!player.isInventoryListenable) return
        val itemstack = event.item ?: return
        val location = event.clickedBlock?.location ?: player.location
        if (!ProtectionManager.canUseItem(player, itemstack, location)) return

        // 该事件比较特殊, 无论是否被“取消”, 总是传递给物品行为
        itemstack.handleInteract(event.player, itemstack, event.action, wrappedEvent)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: PlayerInteractAtEntityEvent) {
        val player = event.player
        if (!player.isInventoryListenable) return
        val itemstack = event.player.inventory.itemInMainHand.takeUnlessEmpty() ?: return

        itemstack.handleInteractAtEntity(event.player, itemstack, event.rightClicked, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onReceive(event: PostprocessDamageEvent) {
        val damagee = event.damagee as? Player ?: return
        if (!damagee.isInventoryListenable) return

        val damageSource = event.damageSource
        for (slot in ItemSlotRegistry.itemSlots()) {
            val itemstack = slot.getItem(damagee) ?: continue
            itemstack.handlePlayerReceiveDamage(damagee, itemstack, damageSource, event)
        }
    }

    @EventHandler()
    fun onAttack(event: PostprocessDamageEvent) {
        val player = event.damageSource.causingEntity as? Player ?: return
        if (!player.isInventoryListenable) return
        val itemstack = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return

        itemstack.handlePlayerAttackEntity(player, itemstack, event.damagee, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: ProjectileLaunchEvent) {
        val projectile = event.entity
        val itemstack = projectile.itemStack ?: return
        val ownerUniqueId = projectile.ownerUniqueId ?: return
        val player = SERVER.getPlayer(ownerUniqueId) ?: return
        if (!player.isInventoryListenable) return

        itemstack.handleItemProjectileLaunch(player, itemstack, projectile, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: ProjectileHitEvent) {
        val projectile = event.entity
        val itemstack = projectile.itemStack ?: return
        val ownerUniqueId = projectile.ownerUniqueId ?: return
        val player = SERVER.getPlayer(ownerUniqueId) ?: return
        if (!player.isInventoryListenable) return

        itemstack.handleItemProjectileHit(player, itemstack, projectile, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: BlockBreakEvent) {
        val player = event.player
        if (!player.isInventoryListenable) return
        val itemstack = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return

        itemstack.handleBreakBlock(player, itemstack, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: PlayerItemDamageEvent) {
        val player = event.player
        if (!player.isInventoryListenable) return
        val itemstack = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return

        itemstack.handleDamage(player, itemstack, event)
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun on(event: PlayerItemBreakEvent) {
        val player = event.player
        if (!player.isInventoryListenable) return
        val itemstack = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return

        itemstack.handleBreak(player, itemstack, event)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        if (!player.isInventoryListenable) return
        val clickedItem = event.currentItem
        val cursorItem = event.cursor.takeUnlessEmpty()

        clickedItem?.handleInventoryClick(player, clickedItem, event)
        cursorItem?.handleInventoryClickOnCursor(player, cursorItem, event)
        if (event.click == ClickType.NUMBER_KEY) {
            val hotbarItem = player.inventory.getItem(event.hotbarButton)
            hotbarItem?.handleInventoryHotbarSwap(player, hotbarItem, event)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun on(event: PlayerStopUsingItemEvent) {
        val player = event.player
        if (!player.isInventoryListenable) return
        val itemstack = event.item.takeUnlessEmpty() ?: return

        itemstack.handleRelease(player, itemstack, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: PlayerItemConsumeEvent) {
        val player = event.player
        if (!player.isInventoryListenable) return
        val itemstack = event.item.takeUnlessEmpty() ?: return

        itemstack.handleConsume(player, itemstack, event)
    }

    private val Projectile.itemStack: ItemStack?
        get() = when (this) {
            is AbstractArrow -> itemStack
            is ThrowableProjectile -> item
            else -> null
        }

}


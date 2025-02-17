@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ability.AbilityEntryPointHandler
import cc.mewcraft.wakame.event.bukkit.NekoEntityDamageEvent
import cc.mewcraft.wakame.event.bukkit.PlayerAbilityPrepareCastEvent
import cc.mewcraft.wakame.event.bukkit.PlayerItemSlotChangeEvent
import cc.mewcraft.wakame.integration.protection.ProtectionManager
import cc.mewcraft.wakame.item.logic.ItemSlotChangeEventListenerRegistry
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.player.equipment.ArmorChangeEvent
import cc.mewcraft.wakame.player.interact.WrappedPlayerInteractEvent
import cc.mewcraft.wakame.user.toUser
import cc.mewcraft.wakame.util.event
import cc.mewcraft.wakame.util.takeUnlessEmpty
import io.papermc.paper.event.player.PlayerStopUsingItemEvent
import org.bukkit.Bukkit
import org.bukkit.entity.*
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack

@Init(
    stage = InitStage.POST_WORLD,
)
object ItemListener {

    @InitFun
    fun init() {
        registerItemChangeListeners()
        registerItemBehaviorListeners()
        registerAbilityEntryPointListeners()
    }

    /**
     * 监听玩家物品栏中的物品所发生的变化.
     * 例如: 物品栏中的物品被更新, 替换, 移动, 丢弃等.
     * 这些事件里都有 “新/旧物品” 两个变量, 用来表示变化前后的物品.
     */
    private fun registerItemChangeListeners() {
        event<PlayerItemSlotChangeEvent> { event ->
            for (listener in ItemSlotChangeEventListenerRegistry.listeners) {
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
     * 监听物品与世界发生的交互事件.
     * 这些都是 *物品行为* 的一部分.
     */
    private fun registerItemBehaviorListeners() {
        event<ArmorChangeEvent>(EventPriority.HIGHEST, true) { event ->
            val player = event.player
            if (!player.isHandleableByKoish) return@event
            val previous = event.previous?.takeUnlessEmpty()
            val current = event.current?.takeUnlessEmpty()

            previous?.shadowNeko()?.handleEquip(player, previous, false, event)
            current?.shadowNeko()?.handleEquip(player, current, true, event)
        }

        event<WrappedPlayerInteractEvent>(EventPriority.NORMAL) { wrappedEvent ->
            val event = wrappedEvent.event
            val player = event.player
            if (!player.isHandleableByKoish) return@event
            val itemStack = event.item ?: return@event
            val nekoStack = itemStack.shadowNeko() ?: return@event
            val location = event.clickedBlock?.location ?: player.location
            if (!ProtectionManager.canUseItem(player, itemStack, location)) return@event

            // 该事件比较特殊, 无论是否被“取消”, 总是传递给物品行为
            nekoStack.handleInteract(event.player, itemStack, event.action, wrappedEvent)
        }

        event<PlayerInteractAtEntityEvent>(EventPriority.HIGHEST, true) { event ->
            val player = event.player
            if (!player.isHandleableByKoish) return@event
            val itemStack = event.player.inventory.itemInMainHand.takeUnlessEmpty()
            val nekoStack = itemStack?.shadowNeko() ?: return@event

            nekoStack.handleInteractAtEntity(event.player, itemStack, event.rightClicked, event)
        }

        event<NekoEntityDamageEvent> { event ->
            val player = event.damageSource.causingEntity as? Player ?: return@event
            if (!player.isHandleableByKoish) return@event
            val itemStack = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return@event
            val nekoStack = itemStack.shadowNeko() ?: return@event

            nekoStack.handleAttackEntity(player, itemStack, event.damagee, event)
        }

        event<ProjectileLaunchEvent>(EventPriority.HIGHEST, true) { event ->
            val projectile = event.entity
            val itemStack = projectile.itemStack ?: return@event
            val ownerUniqueId = projectile.ownerUniqueId ?: return@event
            val player = Bukkit.getPlayer(ownerUniqueId) ?: return@event
            if (!player.isHandleableByKoish) return@event

            itemStack.shadowNeko()?.handleItemProjectileLaunch(player, itemStack, projectile, event)
        }

        event<ProjectileHitEvent>(EventPriority.HIGHEST, true) { event ->
            val projectile = event.entity
            val itemStack = projectile.itemStack ?: return@event
            val ownerUniqueId = projectile.ownerUniqueId ?: return@event
            val player = Bukkit.getPlayer(ownerUniqueId) ?: return@event
            if (!player.isHandleableByKoish) return@event

            itemStack.shadowNeko()?.handleItemProjectileHit(player, itemStack, projectile, event)
        }

        event<BlockBreakEvent>(EventPriority.HIGHEST, true) { event ->
            val player = event.player
            if (!player.isHandleableByKoish) return@event
            val itemStack = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return@event
            val nekoStack = itemStack.shadowNeko() ?: return@event

            nekoStack.handleBreakBlock(player, itemStack, event)
        }

        event<PlayerItemDamageEvent>(EventPriority.HIGHEST, true) { event ->
            val player = event.player
            if (!player.isHandleableByKoish) return@event
            val itemStack = event.item.takeUnlessEmpty() ?: return@event
            val nekoStack = itemStack.shadowNeko() ?: return@event

            nekoStack.handleDamage(player, itemStack, event)
        }

        event<PlayerItemBreakEvent>(EventPriority.HIGH) { event ->
            val player = event.player
            if (!player.isHandleableByKoish) return@event
            val itemStack = event.brokenItem.takeUnlessEmpty() ?: return@event
            val nekoStack = itemStack.shadowNeko() ?: return@event

            nekoStack.handleBreak(player, itemStack, event)
        }

        event<InventoryClickEvent>(EventPriority.HIGH, true) { event ->
            val player = event.whoClicked as Player
            if (!player.isHandleableByKoish) return@event
            val clickedItem = event.currentItem
            val cursorItem = event.cursor.takeUnlessEmpty()

            clickedItem?.shadowNeko()?.handleInventoryClick(player, clickedItem, event)
            cursorItem?.shadowNeko()?.handleInventoryClickOnCursor(player, cursorItem, event)
            if (event.click == ClickType.NUMBER_KEY) {
                val hotbarItem = player.inventory.getItem(event.hotbarButton)
                hotbarItem?.shadowNeko()?.handleInventoryHotbarSwap(player, hotbarItem, event)
            }
        }

        event<PlayerStopUsingItemEvent>(EventPriority.HIGHEST) { event ->
            val player = event.player
            if (!player.isHandleableByKoish) return@event
            val itemStack = event.item.takeUnlessEmpty() ?: return@event
            val nekoStack = itemStack.shadowNeko() ?: return@event

            nekoStack.handleRelease(player, itemStack, event)
        }

        event<PlayerItemConsumeEvent>(EventPriority.HIGHEST, true) { event ->
            val player = event.player
            if (!player.isHandleableByKoish) return@event
            val itemStack = event.item.takeUnlessEmpty() ?: return@event
            val nekoStack = itemStack.shadowNeko() ?: return@event

            nekoStack.handleConsume(player, itemStack, event)
        }

        event<PlayerAbilityPrepareCastEvent>(EventPriority.HIGHEST, true) { event ->
            val player = event.caster
            if (!player.isHandleableByKoish) return@event
            val itemStack = event.item ?: return@event
            val nekoStack = itemStack.shadowNeko() ?: return@event

            nekoStack.handleAbilityPrepareCast(player, itemStack, event.ability, event)
        }
    }

    private fun registerAbilityEntryPointListeners() {
        event<PlayerInteractEvent> { event ->
            val slot = event.hand ?: return@event
            val player = event.player
            if (!player.isHandleableByKoish) return@event
            val itemStack = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return@event
            val nekoStack = itemStack.shadowNeko() ?: return@event
            if (!nekoStack.slotGroup.test(slot)) return@event

            when (event.action) {
                Action.LEFT_CLICK_BLOCK -> AbilityEntryPointHandler.onLeftClickBlock(player, event)
                Action.LEFT_CLICK_AIR -> AbilityEntryPointHandler.onLeftClickAir(player, event)
                Action.RIGHT_CLICK_BLOCK -> AbilityEntryPointHandler.onRightClickBlock(player, event)
                Action.RIGHT_CLICK_AIR -> AbilityEntryPointHandler.onRightClickAir(player, event)
                else -> return@event
            }
        }

        event<EntityDamageByEntityEvent> { event ->
            val damager = event.damager as? Player ?: return@event
            if (!damager.isHandleableByKoish) return@event
            val entity = event.entity as? LivingEntity ?: return@event
            val itemStack = damager.inventory.itemInMainHand.takeUnlessEmpty() ?: return@event

            AbilityEntryPointHandler.onAttack(damager, itemStack, event)
        }

        event<ProjectileHitEvent> { event ->
            AbilityEntryPointHandler.onProjectileHit(event.entity, event.hitEntity)
        }
    }

    /**
     * 萌芽是否应该处理该玩家?
     *
     * 当玩家背包不可监听时, 萌芽不应该处理该与玩家相关的物品行为.
     * 此时, 玩家背包内的任何物品都应该看作是空气, 不提供任何效果.
     */
    private val Player.isHandleableByKoish: Boolean
        get() = toUser().isInventoryListenable


    private val Projectile.itemStack: ItemStack?
        get() = when (this) {
            is AbstractArrow -> itemStack
            is ThrowableProjectile -> item
            else -> null
        }

}

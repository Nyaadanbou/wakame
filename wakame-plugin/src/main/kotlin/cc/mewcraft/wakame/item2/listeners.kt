package cc.mewcraft.wakame.item2

import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.entity.player.isInventoryListenable
import cc.mewcraft.wakame.event.bukkit.PostprocessDamageEvent
import cc.mewcraft.wakame.extensions.toVector3d
import cc.mewcraft.wakame.item2.behavior.AttackContext
import cc.mewcraft.wakame.item2.behavior.AttackOnContext
import cc.mewcraft.wakame.item2.behavior.BlockInteractContext
import cc.mewcraft.wakame.item2.behavior.InteractionHand
import cc.mewcraft.wakame.item2.behavior.InteractionResult
import cc.mewcraft.wakame.item2.behavior.UseContext
import cc.mewcraft.wakame.item2.behavior.UseOnContext
import cc.mewcraft.wakame.item2.behavior.isInteractable
import cc.mewcraft.wakame.item2.behavior.isSuccess
import cc.mewcraft.wakame.item2.behavior.shouldCancel
import cc.mewcraft.wakame.item2.config.property.impl.ItemSlotRegistry
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.item.takeUnlessEmpty
import cc.mewcraft.wakame.util.registerEvents
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent
import io.papermc.paper.event.player.PlayerStopUsingItemEvent
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import org.bukkit.GameMode
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.ThrowableProjectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.EquipmentSlot
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

    // 记录本tick内成功结算交互的玩家
    @JvmStatic
    private val alreadySuccessfulUsePlayers: ReferenceOpenHashSet<Player> = ReferenceOpenHashSet()
    private val alreadySuccessfulAttackPlayers: ReferenceOpenHashSet<Player> = ReferenceOpenHashSet()

    @EventHandler
    fun onServerTick(event: ServerTickStartEvent) {
        alreadySuccessfulUsePlayers.clear()
        alreadySuccessfulAttackPlayers.clear()
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onRightClickInteract(event: PlayerInteractEvent) {
        val action = event.action
        val player = event.player
        // 玩家背包暂时不可监听(可能是在跨服同步) - 不处理
        if (!player.isInventoryListenable) return

        // 玩家处于旁观者模式 - 不处理
        // 玩家不是右键交互 - 不处理
        if (player.gameMode == GameMode.SPECTATOR) return
        if (!action.isRightClick) return

        // 客户端可能会按照自己的想法发送多个交互包, 常见情况为主手与副手各发一次
        // 因此限制每tick只能成功交互一次, 并取消多余的交互事件
        // 但由于网络延迟等原因, 主副手交互事件可能不在同一刻触发
        // 此时玩家眼中为双手物品都成功进行了交互, 该问题无法完美解决
        if (alreadySuccessfulUsePlayers.contains(player)) {
            event.isCancelled = true
            return
        }

        val hand = when (event.hand) {
            EquipmentSlot.HAND -> InteractionHand.MAIN_HAND
            EquipmentSlot.OFF_HAND -> InteractionHand.OFF_HAND
            else -> return
        }
        val itemStack = event.item?.takeUnlessEmpty() ?: return

        if (action == Action.RIGHT_CLICK_AIR) {
            // 玩家交互空气
            val useContext = UseContext(player, hand, itemStack)
            itemStack.handleBehavior { behavior ->
                val result = behavior.handleUse(useContext)
                if (result.isSuccess()) {
                    alreadySuccessfulUsePlayers.add(player)
                }
                if (result.shouldCancel()) {
                    event.isCancelled = true
                }
                if (result != InteractionResult.PASS) {
                    return
                }
            }
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            // 玩家交互方块
            // 方块不存在 - 不处理, 但该情况不可能发生
            val block = event.clickedBlock ?: return
            // 交互位置不存在 - 不处理, 但该情况不可能发生
            val interactPoint = event.interactionPoint ?: return

            val blockInteractContext = BlockInteractContext(
                block.location.toVector3d(),
                event.blockFace,
                interactPoint.toVector3d()
            )
            // 判定方块是否可以交互
            val interactable = block.isInteractable(player, itemStack, blockInteractContext)
            // 方块本身可交互且玩家为非潜行状态 - 不处理
            // 此时应优先触发方块交互, 不执行物品的自定义交互
            // 反过来也就是说, 玩家潜行或是方块本身不可交互时, 才尝试执行物品的交互
            if (!player.isSneaking && interactable) return
            // 遍历物品上的所有行为, 依次执行所有 handleUseOn 逻辑
            // 执行顺序取决于注册顺序, 因此用需要取消后续行为的行为应更早注册
            val useOnContext = UseOnContext(player, hand, itemStack, blockInteractContext)
            itemStack.handleBehavior { behavior ->
                val result = behavior.handleUseOn(useOnContext)
                if (result.isSuccess()) {
                    alreadySuccessfulUsePlayers.add(player)
                }
                if (result.shouldCancel()) {
                    event.isCancelled = true
                }
                if (result != InteractionResult.PASS) {
                    return
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onLeftClickInteract(event: PlayerInteractEvent) {
        val action = event.action
        val player = event.player
        // 玩家背包暂时不可监听(可能是在跨服同步) - 不处理
        if (!player.isInventoryListenable) return

        // 玩家处于旁观者模式 - 不处理
        // 玩家不是左键交互 - 不处理
        if (player.gameMode == GameMode.SPECTATOR) return
        if (!action.isLeftClick) return

        // 限制每tick只能成功交互一次, 并取消多余的交互事件
        if (alreadySuccessfulUsePlayers.contains(player)) {
            event.isCancelled = true
            return
        }

        // 左键交互只可能是主手
        if (event.hand != EquipmentSlot.HAND)  return
        val itemStack = event.item?.takeUnlessEmpty() ?: return

        if (action == Action.LEFT_CLICK_AIR) {
            // 玩家交互空气
            val attackContext = AttackContext(player, itemStack)
            itemStack.handleBehavior { behavior ->
                val result = behavior.handleAttack(attackContext)
                if (result.isSuccess()) {
                    alreadySuccessfulAttackPlayers.add(player)
                }
                if (result.shouldCancel()) {
                    event.isCancelled = true
                }
                if (result != InteractionResult.PASS) {
                    return
                }
            }
        } else if (action == Action.LEFT_CLICK_BLOCK) {
            // 玩家交互方块
            // 方块不存在 - 不处理, 但该情况不可能发生
            val block = event.clickedBlock ?: return
            // 交互位置不存在 - 不处理, 但该情况不可能发生
            val interactPoint = event.interactionPoint ?: return

            val blockInteractContext = BlockInteractContext(
                block.location.toVector3d(),
                event.blockFace,
                interactPoint.toVector3d()
            )
            val attackOnContext = AttackOnContext(player, itemStack, blockInteractContext)
            itemStack.handleBehavior { behavior ->
                val result = behavior.handleAttackOn(attackOnContext)
                if (result.isSuccess()) {
                    alreadySuccessfulAttackPlayers.add(player)
                }
                if (result.shouldCancel()) {
                    event.isCancelled = true
                }
                if (result != InteractionResult.PASS) {
                    return
                }
            }
        }
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


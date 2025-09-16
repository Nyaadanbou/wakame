package cc.mewcraft.wakame.item2

import cc.mewcraft.wakame.SERVER
import cc.mewcraft.wakame.entity.player.isInventoryListenable
import cc.mewcraft.wakame.event.bukkit.PostprocessDamageEvent
import cc.mewcraft.wakame.extensions.toVector3d
import cc.mewcraft.wakame.item2.behavior.AttackContext
import cc.mewcraft.wakame.item2.behavior.AttackEntityContext
import cc.mewcraft.wakame.item2.behavior.AttackOnContext
import cc.mewcraft.wakame.item2.behavior.BehaviorResult
import cc.mewcraft.wakame.item2.behavior.BlockInteractContext
import cc.mewcraft.wakame.item2.behavior.CauseDamageContext
import cc.mewcraft.wakame.item2.behavior.ConsumeContext
import cc.mewcraft.wakame.item2.behavior.DurabilityDecreaseContext
import cc.mewcraft.wakame.item2.behavior.InteractionHand
import cc.mewcraft.wakame.item2.behavior.InteractionResult
import cc.mewcraft.wakame.item2.behavior.ItemStackActivationChecker.isActive
import cc.mewcraft.wakame.item2.behavior.ProjectileHitContext
import cc.mewcraft.wakame.item2.behavior.ProjectileLaunchContext
import cc.mewcraft.wakame.item2.behavior.ReceiveDamageContext
import cc.mewcraft.wakame.item2.behavior.StopUseContext
import cc.mewcraft.wakame.item2.behavior.UseContext
import cc.mewcraft.wakame.item2.behavior.UseEntityContext
import cc.mewcraft.wakame.item2.behavior.UseOnContext
import cc.mewcraft.wakame.item2.behavior.handleBehavior
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
import io.papermc.paper.event.player.PlayerStopUsingItemEvent
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
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
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
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

    @JvmStatic
    private val alreadySuccessfulUsePlayers: ReferenceOpenHashSet<Player> = ReferenceOpenHashSet()
    private val alreadySuccessfulAttackPlayers: ReferenceOpenHashSet<Player> = ReferenceOpenHashSet()
    private val alreadyCallInteractEntityWithMainHandPlayers: ReferenceOpenHashSet<Player> = ReferenceOpenHashSet()
    private val alreadyCallInteractEntityWithOffHandPlayers: ReferenceOpenHashSet<Player> = ReferenceOpenHashSet()

    @EventHandler
    fun onServerTick(event: ServerTickStartEvent) {
        alreadySuccessfulUsePlayers.clear()
        alreadySuccessfulAttackPlayers.clear()
        alreadyCallInteractEntityWithMainHandPlayers.clear()
        alreadyCallInteractEntityWithOffHandPlayers.clear()
    }

    @EventHandler(priority = EventPriority.LOW)
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
        // 玩家手中没有物品 - 不处理
        val itemStack = event.item?.takeUnlessEmpty() ?: return

        if (action == Action.RIGHT_CLICK_AIR) {
            // 玩家交互空气
            // 若玩家已右键交互实体 - 不处理
            // 避免同时触发 Use 和 UseEntity 行为
            if (hand == InteractionHand.MAIN_HAND) {
                if (alreadyCallInteractEntityWithMainHandPlayers.contains(player)) {
                    event.isCancelled = true
                    return
                }
            } else {
                if (alreadyCallInteractEntityWithOffHandPlayers.contains(player)) {
                    event.isCancelled = true
                    return
                }
            }
            val useContext = UseContext(player, itemStack, hand)
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
            val useOnContext = UseOnContext(player, itemStack, hand, blockInteractContext)
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

    @EventHandler(priority = EventPriority.LOW)
    fun onInteractEntity(event: PlayerInteractEntityEvent) {
        val player = event.player
        // 玩家背包暂时不可监听(可能是在跨服同步) - 不处理
        if (!player.isInventoryListenable) return

        // 玩家处于旁观者模式 - 不处理
        if (player.gameMode == GameMode.SPECTATOR) return

        // 限制每tick只能成功交互一次, 并取消多余的交互事件
        if (alreadySuccessfulUsePlayers.contains(player)) {
            event.isCancelled = true
            return
        }

        val hand = when (event.hand) {
            EquipmentSlot.HAND -> InteractionHand.MAIN_HAND
            EquipmentSlot.OFF_HAND -> InteractionHand.OFF_HAND
            else -> return
        }
        // 玩家手中没有物品 - 不处理
        val itemStack = if (hand == InteractionHand.MAIN_HAND) {
            alreadyCallInteractEntityWithMainHandPlayers.add(player)
            player.inventory.itemInMainHand
        } else {
            alreadyCallInteractEntityWithOffHandPlayers.add(player)
            player.inventory.itemInOffHand
        }.takeUnlessEmpty() ?: return
        val entity = event.rightClicked

        // 判定玩家手中的物品和实体是否存在原版交互
        // 实体存在原版交互 - 不处理
        if (entity.isInteractable(player, itemStack)) return
        val useEntityContext = UseEntityContext(player, itemStack, hand, entity)
        itemStack.handleBehavior { behavior ->
            val result = behavior.handleUseEntity(useEntityContext)
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

    @EventHandler(priority = EventPriority.LOW)
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
        if (alreadySuccessfulAttackPlayers.contains(player)) {
            event.isCancelled = true
            return
        }

        // 左键交互只可能是主手
        if (event.hand != EquipmentSlot.HAND) return
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

            val attackOnContext = AttackOnContext(player, itemStack, block.location.toVector3d(), event.blockFace)
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

    @EventHandler(priority = EventPriority.LOW)
    fun onPreAttack(event: PrePlayerAttackEntityEvent) {
        val player = event.player
        // 玩家背包暂时不可监听(可能是在跨服同步) - 不处理
        if (!player.isInventoryListenable) return

        // 玩家处于旁观者模式 - 不处理
        if (player.gameMode == GameMode.SPECTATOR) return

        // 限制每tick只能成功交互一次, 并取消多余的交互事件
        if (alreadySuccessfulAttackPlayers.contains(player)) {
            event.isCancelled = true
            return
        }

        val itemStack = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return
        val attackEntityContext = AttackEntityContext(player, itemStack, event.attacked)
        itemStack.handleBehavior { behavior ->
            val result = behavior.handleAttackEntity(attackEntityContext)
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

    @EventHandler(priority = EventPriority.LOW)
    fun onPostprocessDamage(event: PostprocessDamageEvent) {
        val causingEntity = event.damageSource.causingEntity
        if (causingEntity is Player) {
            // 确保此时玩家的背包可以监听
            if (!causingEntity.isInventoryListenable) return
            // 遍历所有 Koish 关心的槽位
            for (slot in ItemSlotRegistry.itemSlots()) {
                // 该槽位没有物品 - 跳转至下一循环
                val itemStack = slot.getItem(causingEntity) ?: continue
                // 该槽位的物品不处于激活状态 - 跳转至下一循环
                if (!itemStack.isActive(slot, causingEntity)) continue

                val context = CauseDamageContext(causingEntity, itemStack, event.damagee, event.finalDamageContext)
                itemStack.handleBehavior { behavior ->
                    val result = behavior.handleCauseDamage(context)
                    if (result == BehaviorResult.FINISH_AND_CANCEL) {
                        event.isCancelled = true
                    }
                    if (result != BehaviorResult.PASS) {
                        return
                    }
                }
            }
        }

        val damagee = event.damagee
        if (damagee is Player) {
            // 确保此时玩家的背包可以监听
            if (!damagee.isInventoryListenable) return
            // 遍历所有 Koish 关心的槽位
            for (slot in ItemSlotRegistry.itemSlots()) {
                // 该槽位没有物品 - 跳转至下一循环
                val itemStack = slot.getItem(damagee) ?: continue
                // 该槽位的物品不处于激活状态 - 跳转至下一循环
                if (!itemStack.isActive(slot, damagee)) continue

                val context = ReceiveDamageContext(damagee, itemStack, event.damageSource, event.finalDamageContext)
                itemStack.handleBehavior { behavior ->
                    val result = behavior.handleReceiveDamage(context)
                    if (result == BehaviorResult.FINISH_AND_CANCEL) {
                        event.isCancelled = true
                    }
                    if (result != BehaviorResult.PASS) {
                        return
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onProjectileLaunch(event: ProjectileLaunchEvent) {
        val projectile = event.entity
        // 弹射物实体的主人UUID不存在 - 不处理
        val ownerUniqueId = projectile.ownerUniqueId ?: return
        // 发射弹射物的玩家不存在(玩家不在线) - 不处理
        val player = SERVER.getPlayer(ownerUniqueId) ?: return
        // 确保此时玩家的背包可以监听
        if (!player.isInventoryListenable) return
        // 无法获取弹射物实体对应的物品 - 不处理
        val itemStack = projectile.itemStack ?: return

        val context = ProjectileLaunchContext(player, itemStack, projectile)
        itemStack.handleBehavior { behavior ->
            val result = behavior.handleProjectileLaunch(context)
            if (result == BehaviorResult.FINISH_AND_CANCEL) {
                event.isCancelled = true
            }
            if (result != BehaviorResult.PASS) {
                return
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onProjectileHit(event: ProjectileHitEvent) {
        val projectile = event.entity
        // 弹射物实体的主人UUID不存在 - 不处理
        val ownerUniqueId = projectile.ownerUniqueId ?: return
        // 发射弹射物的玩家不存在(玩家不在线) - 不处理
        val player = SERVER.getPlayer(ownerUniqueId) ?: return
        // 确保此时玩家的背包可以监听
        if (!player.isInventoryListenable) return
        // 无法获取弹射物实体对应的物品 - 不处理
        val itemStack = projectile.itemStack ?: return

        val context = ProjectileHitContext(player, itemStack, projectile, event.hitEntity, event.hitBlock, event.hitBlockFace)
        itemStack.handleBehavior { behavior ->
            val result = behavior.handleProjectileHit(context)
            if (result == BehaviorResult.FINISH_AND_CANCEL) {
                event.isCancelled = true
            }
            if (result != BehaviorResult.PASS) {
                return
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun on(event: PlayerItemDamageEvent) {
        val player = event.player
        // 确保此时玩家的背包可以监听
        if (!player.isInventoryListenable) return
        val itemStack = event.item

        val context = DurabilityDecreaseContext(player, itemStack, event.damage, event.originalDamage)
        itemStack.handleBehavior { behavior ->
            val result = behavior.handleDurabilityDecrease(context)
            if (result == BehaviorResult.FINISH_AND_CANCEL) {
                event.isCancelled = true
            }
            if (context.willModifyDurabilityDecreaseValue) {
                event.damage = context.newDurabilityDecreaseValue
            }
            if (result != BehaviorResult.PASS) {
                return
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun on(event: PlayerStopUsingItemEvent) {
        val player = event.player
        // 确保此时玩家的背包可以监听
        if (!player.isInventoryListenable) return
        val itemStack = event.item

        val context = StopUseContext(player, itemStack, event.ticksHeldFor)
        itemStack.handleBehavior { behavior ->
            val result = behavior.handleStopUse(context)
            if (result != BehaviorResult.PASS) {
                return
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: PlayerItemConsumeEvent) {
        val player = event.player
        // 确保此时玩家的背包可以监听
        if (!player.isInventoryListenable) return
        val itemStack = event.item
        val hand = when (event.hand) {
            EquipmentSlot.HAND -> InteractionHand.MAIN_HAND
            EquipmentSlot.OFF_HAND -> InteractionHand.OFF_HAND
            else -> return
        }

        val context = ConsumeContext(player, itemStack, hand, event.replacement)
        itemStack.handleBehavior { behavior ->
            val result = behavior.handleConsume(context)
            if (result == BehaviorResult.FINISH_AND_CANCEL) {
                event.isCancelled = true
            }
            if (context.willModifyReplacement) {
                event.replacement = context.newReplacement
            }
            if (result != BehaviorResult.PASS) {
                return
            }
        }
    }

    private val Projectile.itemStack: ItemStack?
        get() = when (this) {
            is AbstractArrow -> itemStack
            is ThrowableProjectile -> item
            else -> null
        }

}


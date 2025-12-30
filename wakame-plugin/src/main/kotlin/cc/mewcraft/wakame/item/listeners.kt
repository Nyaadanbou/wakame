package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.entity.player.isInventoryListenable
import cc.mewcraft.wakame.event.bukkit.PostprocessDamageEvent
import cc.mewcraft.wakame.item.behavior.*
import cc.mewcraft.wakame.item.behavior.ItemStackActivationChecker.isActive
import cc.mewcraft.wakame.item.property.impl.ItemSlotRegistry
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.util.item.takeUnlessEmpty
import cc.mewcraft.wakame.util.registerEvents
import cc.mewcraft.wakame.util.toVector3d
import com.destroystokyo.paper.event.server.ServerTickStartEvent
import io.papermc.paper.event.player.PlayerStopUsingItemEvent
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.EquipmentSlot

@Init(stage = InitStage.POST_WORLD)
internal object ItemBehaviorListener : Listener {

    @InitFun
    fun init() {
        registerEvents()
    }

    @JvmStatic
    private val alreadySuccessfulUsePlayers: ReferenceOpenHashSet<Player> = ReferenceOpenHashSet()
    @JvmStatic
    private val alreadySuccessfulAttackPlayers: ReferenceOpenHashSet<Player> = ReferenceOpenHashSet()
    @JvmStatic
    private val alreadyCallInteractEntityWithMainHandPlayers: ReferenceOpenHashSet<Player> = ReferenceOpenHashSet()
    @JvmStatic
    private val alreadyCallInteractEntityWithOffHandPlayers: ReferenceOpenHashSet<Player> = ReferenceOpenHashSet()

    @EventHandler
    fun onServerTick(event: ServerTickStartEvent) {
        alreadySuccessfulUsePlayers.clear()
        alreadySuccessfulAttackPlayers.clear()
        alreadyCallInteractEntityWithMainHandPlayers.clear()
        alreadyCallInteractEntityWithOffHandPlayers.clear()
    }

    // Koish 的交互应该在最后的最后触发.
    // 取消事件只是为了移除原版交互的影响, 而不是通知其他插件,
    // 故使用 EventPriority.MONITOR, 下同.
    @EventHandler(priority = EventPriority.MONITOR)
    fun onRightClickInteract(event: PlayerInteractEvent) {
        // 2025/9/22 芙兰
        // 吗的 Bukkit 这交互事件是人啊

        // 事件被取消 - 不处理
        // 不能用 isCancelled 或者 ignoreCancelled = true 判定
        // 原因是 PlayerInteractEvent 的 isCancelled 仅仅判断了 useInteractedBlock()
        // 玩家交互空气时该函数返回值必然为 Event.Result.DENY, 也就是事件必然是取消状态
        if (event.useInteractedBlock() == Event.Result.DENY && event.useItemInHand() == Event.Result.DENY) {
            return
        }

        val action = event.action
        val player = event.player
        if (!player.isInventoryListenable) return // 玩家背包暂时不可监听(可能是在跨服同步) - 不处理
        if (player.gameMode == GameMode.SPECTATOR) return // 玩家处于旁观者模式 - 不处理
        if (!action.isRightClick) return // 玩家不是右键交互 - 不处理

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
        val itemstack = event.item?.takeUnlessEmpty() ?: return // 玩家手中没有物品 - 不处理

        if (action == Action.RIGHT_CLICK_AIR) { // 玩家交互空气
            if (hand == InteractionHand.MAIN_HAND) { // 若玩家已右键交互实体 - 不处理, 避免同时触发 Use 和 UseEntity 行为
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
            val useContext = UseContext(player, itemstack, hand)
            itemstack.handleBehavior { behavior ->
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
        } else if (action == Action.RIGHT_CLICK_BLOCK) { // 玩家交互方块
            val block = event.clickedBlock ?: return // 方块不存在 - 不处理, 但该情况不可能发生
            val interactPoint = event.interactionPoint ?: return // 交互位置不存在 - 不处理, 但该情况不可能发生
            val blockInteractContext = BlockInteractContext(
                block.location.toVector3d(),
                event.blockFace,
                interactPoint.toVector3d()
            )
            // 判定方块是否可以交互
            val interactable = block.isInteractable(player, itemstack, blockInteractContext)
            // 方块本身可交互且玩家为非潜行状态 - 不处理
            // 此时应优先触发方块交互, 不执行物品的自定义交互
            // 反过来也就是说, 玩家潜行或是方块本身不可交互时, 才尝试执行物品的交互
            if (!player.isSneaking && interactable) return
            // 遍历物品上的所有行为, 依次执行所有 handleUseOn 逻辑
            // 执行顺序取决于注册顺序, 因此用需要取消后续行为的行为应更早注册
            val useOnContext = UseOnContext(player, itemstack, hand, blockInteractContext)
            itemstack.handleBehavior { behavior ->
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onInteractEntity(event: PlayerInteractEntityEvent) {
        val player = event.player
        if (!player.isInventoryListenable) return // 玩家背包暂时不可监听(可能是在跨服同步) - 不处理
        if (player.gameMode == GameMode.SPECTATOR) return // 玩家处于旁观者模式 - 不处理
        if (alreadySuccessfulUsePlayers.contains(player)) { // 限制每 tick 只能成功交互一次, 并取消多余的交互事件
            event.isCancelled = true
            return
        }

        val hand = when (event.hand) {
            EquipmentSlot.HAND -> InteractionHand.MAIN_HAND
            EquipmentSlot.OFF_HAND -> InteractionHand.OFF_HAND
            else -> return
        }
        val itemstack = if (hand == InteractionHand.MAIN_HAND) {
            alreadyCallInteractEntityWithMainHandPlayers.add(player)
            player.inventory.itemInMainHand
        } else {
            alreadyCallInteractEntityWithOffHandPlayers.add(player)
            player.inventory.itemInOffHand
        }.takeUnlessEmpty() ?: return // 玩家手中没有物品 - 不处理
        val entity = event.rightClicked

        // 判定玩家手中的物品和实体是否存在原版交互
        if (entity.isInteractable(player, itemstack)) return // 实体存在原版交互 - 不处理
        val useEntityContext = UseEntityContext(player, itemstack, hand, entity)
        itemstack.handleBehavior { behavior ->
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

    @EventHandler(priority = EventPriority.MONITOR)
    fun onLeftClickInteract(event: PlayerInteractEvent) {
        // 事件被取消 - 不处理
        // 这样写的原因同上文的 onRightClickInteract(...)
        if (event.useInteractedBlock() == Event.Result.DENY && event.useItemInHand() == Event.Result.DENY) {
            return
        }

        val action = event.action
        val player = event.player
        if (!player.isInventoryListenable) return // 玩家背包暂时不可监听(可能是在跨服同步) - 不处理
        if (player.gameMode == GameMode.SPECTATOR) return // 玩家处于旁观者模式 - 不处理
        if (!action.isLeftClick) return // 玩家不是左键交互 - 不处理
        if (alreadySuccessfulAttackPlayers.contains(player)) { // 限制每tick只能成功交互一次, 并取消多余的交互事件
            event.isCancelled = true
            return
        }
        if (event.hand != EquipmentSlot.HAND) return // 左键交互只可能是主手
        val itemstack = event.item?.takeUnlessEmpty() ?: return

        if (action == Action.LEFT_CLICK_AIR) { // 玩家交互空气
            val attackContext = AttackContext(player, itemstack)
            itemstack.handleBehavior { behavior ->
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
        } else if (action == Action.LEFT_CLICK_BLOCK) { // 玩家交互方块
            val block = event.clickedBlock ?: return // 方块不存在 - 不处理, 但该情况不可能发生

            val attackOnContext = AttackOnContext(player, itemstack, block.location.toVector3d(), event.blockFace)
            itemstack.handleBehavior { behavior ->
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
        if (!player.isInventoryListenable) return // 玩家背包暂时不可监听(可能是在跨服同步) - 不处理
        if (player.gameMode == GameMode.SPECTATOR) return // 玩家处于旁观者模式 - 不处理
        if (alreadySuccessfulAttackPlayers.contains(player)) { // 限制每tick只能成功交互一次, 并取消多余的交互事件
            event.isCancelled = true
            return
        }

        val itemstack = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return
        val attackEntityContext = AttackEntityContext(player, itemstack, event.attacked)
        itemstack.handleBehavior { behavior ->
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
            if (!causingEntity.isInventoryListenable) return // 确保此时玩家的背包可以监听
            for (slot in ItemSlotRegistry.itemSlots()) { // 遍历所有 Koish 关心的槽位
                val itemstack = slot.getItem(causingEntity) ?: continue // 该槽位没有物品 - 跳转至下一循环
                if (!itemstack.isActive(slot, causingEntity)) continue // 该槽位的物品不处于激活状态 - 跳转至下一循环

                val context = CauseDamageContext(causingEntity, itemstack, event.damagee, event.finalDamageContext)
                itemstack.handleBehavior { behavior ->
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
            if (!damagee.isInventoryListenable) return // 确保此时玩家的背包可以监听
            for (slot in ItemSlotRegistry.itemSlots()) { // 遍历所有 Koish 关心的槽位
                val itemstack = slot.getItem(damagee) ?: continue // 该槽位没有物品 - 跳转至下一循环
                if (!itemstack.isActive(slot, damagee)) continue // 该槽位的物品不处于激活状态 - 跳转至下一循环

                val context = ReceiveDamageContext(damagee, itemstack, event.damageSource, event.finalDamageContext)
                itemstack.handleBehavior { behavior ->
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
    fun on(event: PlayerItemDamageEvent) {
        val player = event.player
        if (!player.isInventoryListenable) return // 确保此时玩家的背包可以监听
        val itemstack = event.item

        val context = DurabilityDecreaseContext(player, itemstack, event.damage, event.originalDamage)
        itemstack.handleBehavior { behavior ->
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
        if (!player.isInventoryListenable) return // 确保此时玩家的背包可以监听
        val itemstack = event.item

        val context = StopUseContext(player, itemstack, event.ticksHeldFor)
        itemstack.handleBehavior { behavior ->
            val result = behavior.handleStopUse(context)
            if (result != BehaviorResult.PASS) {
                return
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: PlayerItemConsumeEvent) {
        val player = event.player
        if (!player.isInventoryListenable) return // 确保此时玩家的背包可以监听
        val itemstack = event.item
        val hand = when (event.hand) {
            EquipmentSlot.HAND -> InteractionHand.MAIN_HAND
            EquipmentSlot.OFF_HAND -> InteractionHand.OFF_HAND
            else -> return
        }

        val context = ConsumeContext(player, itemstack, hand, event.replacement)
        itemstack.handleBehavior { behavior ->
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
}

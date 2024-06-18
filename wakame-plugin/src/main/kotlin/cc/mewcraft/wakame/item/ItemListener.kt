package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.attribute.AttributeEventHandler
import cc.mewcraft.wakame.event.PlayerInventorySlotChangeEvent
import cc.mewcraft.wakame.event.PlayerSkillPrepareCastEvent
import cc.mewcraft.wakame.item.binary.tryNekoStack
import cc.mewcraft.wakame.kizami.KizamiEventHandler
import cc.mewcraft.wakame.skill.SkillEventHandler
import cc.mewcraft.wakame.util.takeUnlessEmpty
import com.destroystokyo.paper.event.player.PlayerJumpEvent
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 此监听器仅处理 [PlayerItemHeldEvent] 和 [PlayerInventorySlotChangeEvent].
 *
 * ## 为什么需要单独列出来?
 * 简单来说, 这是因为 ItemBehavior 架构的局限性.
 * ItemBehavior 本质是“交互结果”的封装. 我们希望把交互结果封装成一个一个的类,
 * 这样我们就不用把所有交互逻辑全部写在一个 [EventHandler] 里. 应该没有人希望
 * 看到 [PlayerItemHeldEvent] 下有一个几百行的 [EventHandler] 吧.
 *
 * ItemBehavior 运行的流程是这样的:
 *
 * 1. 交互事件发生
 * 2. 获取交互事件中的涉及的物品
 * 3. 获取该物品的所有 ItemBehavior
 * 4. 逐个执行获取到的 ItemBehavior
 *
 * 如果该交互事件只涉及到一个物品, 按照上面的流程就没什么问题.
 * 但是当物品涉及到多个物品时, 上面的流程就存在局限性了.
 *
 *
 *
 * ## [PlayerItemHeldEvent]
 *
 *
 *
 * ## [PlayerInventorySlotChangeEvent]
 *
 * 这里需要更详细的文档说明.
 */
class MultipleItemListener : KoinComponent, Listener {
    private val attributeEventHandler: AttributeEventHandler by inject()
    private val kizamiEventHandler: KizamiEventHandler by inject()
    private val skillEventHandler: SkillEventHandler by inject()

    @EventHandler
    fun onItemHeld(event: PlayerItemHeldEvent) {
        val player = event.player
        val previousSlot = event.previousSlot
        val newSlot = event.newSlot
        val oldItem = player.inventory.getItem(previousSlot) // it returns `null` to represent emptiness
        val newItem = player.inventory.getItem(newSlot) // same as above

        attributeEventHandler.handlePlayerItemHeld(player, previousSlot, newSlot, oldItem, newItem)
        kizamiEventHandler.handlePlayerItemHeld(player, previousSlot, newSlot, oldItem, newItem)
        skillEventHandler.handlePlayerItemHeld(player, previousSlot, newSlot, oldItem, newItem)
    }

    @EventHandler
    fun onSlotChange(event: PlayerInventorySlotChangeEvent) {
        val player = event.player
        val rawSlot = event.rawSlot
        val slot = event.slot
        val oldItem = event.oldItemStack.takeUnlessEmpty() // it always returns a non-null ItemStack - it uses AIR to represent emptiness
        val newItem = event.newItemStack.takeUnlessEmpty() // same as above

        attributeEventHandler.handlePlayerInventorySlotChange(player, rawSlot, slot, oldItem, newItem)
        kizamiEventHandler.handlePlayerInventorySlotChange(player, rawSlot, slot, oldItem, newItem)
        skillEventHandler.handlePlayerInventorySlotChange(player, rawSlot, slot, oldItem, newItem)
    }
}

/**
 * 此监听器包含的事件仅涉及到一个物品.
 */
class SingleItemListener : KoinComponent, Listener {
    private val skillEventHandler: SkillEventHandler by inject()

    @EventHandler
    fun onItemInteract(event: PlayerInteractEvent) {
        val item = event.item ?: return
        val nekoStack = item.tryNekoStack ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleInteract(event.player, item, event.action, event)
        }
    }

    @EventHandler
    fun onDamageEntity(event: EntityDamageByEntityEvent) {
        // TODO: 这是一个 POC，可能需要考虑背包内的所有 Neko 物品
        val damager = event.damager as? Player ?: return
        val item = damager.inventory.itemInMainHand
        val nekoStack = item.tryNekoStack ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleAttackEntity(damager, item, event.entity, event)
        }
    }

    @EventHandler
    fun onItemBreakBlock(event: BlockBreakEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand.takeIf { !it.isEmpty } ?: return
        val nekoStack = item.tryNekoStack ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleBreakBlock(player, item, event)
        }
    }

    @EventHandler
    fun onItemDamage(event: PlayerItemDamageEvent) {
        val item = event.item
        val nekoStack = item.tryNekoStack ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleDamage(event.player, item, event)
        }
    }

    @EventHandler
    fun onItemBreak(event: PlayerItemBreakEvent) {
        val item = event.brokenItem
        val nekoStack = item.tryNekoStack ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleBreak(event.player, item, event)
        }
    }

    @EventHandler
    fun onItemConsume(event: PlayerItemConsumeEvent) {
        val item = event.item
        val nekoStack = item.tryNekoStack ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleConsume(event.player, item, event)
        }
    }

    @EventHandler
    fun onSkillPrepareCast(event: PlayerSkillPrepareCastEvent) {
        val item = event.item
        val nekoStack = item?.tryNekoStack ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleSkillPrepareCast(event.caster, item, event.skill, event)
        }
    }

    @EventHandler
    fun onClick(event: PlayerInteractEvent) {
        val player = event.player
        val slot = event.hand ?: return
        val item = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return
        val nekoStack = item.tryNekoStack ?: return
        if (!nekoStack.slot.testEquipmentSlot(slot))
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
    fun onJump(event: PlayerJumpEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return
        val nekoStack = item.tryNekoStack ?: return

        skillEventHandler.onJump(player, item)
    }

    @EventHandler
    fun onAttack(event: EntityDamageByEntityEvent) {
        val damager = event.damager as? Player ?: return
        val entity = event.entity as? LivingEntity ?: return
        val item = damager.inventory.itemInMainHand.takeUnlessEmpty() ?: return
        val nekoStack = item.tryNekoStack ?: return

        skillEventHandler.onAttack(damager, entity, item)
    }
}
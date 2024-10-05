package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.attribute.AttributeEventHandler
import cc.mewcraft.wakame.enchantment.EnchantmentEventHandler
import cc.mewcraft.wakame.event.PlayerItemSlotChangeEvent
import cc.mewcraft.wakame.event.PlayerSkillPrepareCastEvent
import cc.mewcraft.wakame.kizami.KizamiEventHandler
import cc.mewcraft.wakame.player.attackspeed.AttackSpeedEventHandler
import cc.mewcraft.wakame.player.equipment.ArmorChangeEvent
import cc.mewcraft.wakame.skill.SkillEventHandler
import cc.mewcraft.wakame.util.takeUnlessEmpty
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 监听玩家物品栏中的物品所发生的变化.
 * 这些事件都有 “新/旧物品” 两个变量.
 */
internal class ItemChangeListener : KoinComponent, Listener {
    private val attackSpeedEventHandler: AttackSpeedEventHandler by inject()
    private val attributeEventHandler: AttributeEventHandler by inject()
    private val enchantmentEventHandler: EnchantmentEventHandler by inject()
    private val kizamiEventHandler: KizamiEventHandler by inject()
    private val skillEventHandler: SkillEventHandler by inject()

    @EventHandler
    fun on(event: PlayerItemSlotChangeEvent) {
        val player = event.player
        val itemSlot = event.slot
        val oldItem = event.oldItemStack
        val newItem = event.newItemStack

        attackSpeedEventHandler.handlePlayerSlotChange(player, itemSlot, oldItem, newItem)
        attributeEventHandler.handlePlayerSlotChange(player, itemSlot, oldItem, newItem)
        enchantmentEventHandler.handlePlayerSlotChange(player, itemSlot, oldItem, newItem)
        kizamiEventHandler.handlePlayerSlotChange(player, itemSlot, oldItem, newItem)
        skillEventHandler.handlePlayerSlotChange(player, itemSlot, oldItem, newItem)
    }
}

/**
 * 监听物品与世界发生的交互事件.
 * 这些都是 [cc.mewcraft.wakame.item.behavior.ItemBehavior] 实现的一部分.
 */
internal class ItemBehaviorListener : KoinComponent, Listener {
    @EventHandler
    fun on(event: PlayerInteractEvent) {
        val item = event.item ?: return
        val nekoStack = item.tryNekoStack ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleInteract(event.player, item, event.action, event)
        }
    }

    @EventHandler
    fun on(event: EntityDamageByEntityEvent) {
        val damager = event.damager as? Player ?: return
        val item = damager.inventory.itemInMainHand.takeUnlessEmpty() ?: return
        val nekoStack = item.tryNekoStack ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleAttackEntity(damager, item, event.entity, event)
        }
    }

    @EventHandler
    fun on(event: BlockBreakEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return
        val nekoStack = item.tryNekoStack ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleBreakBlock(player, item, event)
        }
    }

    @EventHandler
    fun on(event: PlayerItemDamageEvent) {
        val item = event.item.takeUnlessEmpty() ?: return
        val nekoStack = item.tryNekoStack ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleDamage(event.player, item, event)
        }
    }

    @EventHandler
    fun on(event: PlayerItemBreakEvent) {
        val item = event.brokenItem.takeUnlessEmpty() ?: return
        val nekoStack = item.tryNekoStack ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleBreak(event.player, item, event)
        }
    }

    @EventHandler
    fun on(event: ArmorChangeEvent) {
        val player = event.player
        val unequippedItem = event.previous
        val equippedItem = event.current

        unequippedItem?.tryNekoStack?.behaviors?.forEach { behavior ->
            behavior.handleEquip(player, unequippedItem, false, event)
        }
        equippedItem?.tryNekoStack?.behaviors?.forEach { behavior ->
            behavior.handleEquip(player, equippedItem, true, event)
        }
    }

    @EventHandler
    fun on(event: PlayerItemSlotChangeEvent) {
        val player = event.player
        val slot = event.slot
        val oldItem = event.oldItemStack
        val newItem = event.newItemStack

        oldItem?.tryNekoStack?.behaviors?.forEach { behavior ->
            behavior.handleSlotChange(player, oldItem, true, slot, event)
        }
        newItem?.tryNekoStack?.behaviors?.forEach { behavior ->
            behavior.handleSlotChange(player, newItem, false, slot, event)
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        val clickedItem = event.currentItem
        val cursorItem = event.cursor.takeUnlessEmpty()

        clickedItem?.tryNekoStack?.behaviors?.forEach { behavior ->
            behavior.handleInventoryClick(player, clickedItem, event)
        }
        cursorItem?.tryNekoStack?.behaviors?.forEach { behavior ->
            behavior.handleInventoryClickOnCursor(player, cursorItem, event)
        }

        if (event.click == ClickType.NUMBER_KEY) {
            val hotbarItem = player.inventory.getItem(event.hotbarButton)
            hotbarItem?.tryNekoStack?.behaviors?.forEach { behavior ->
                behavior.handleInventoryHotbarSwap(player, hotbarItem, event)
            }
        }
    }

    @EventHandler
    fun on(event: PlayerItemConsumeEvent) {
        val item = event.item.takeUnlessEmpty() ?: return
        val nekoStack = item.tryNekoStack ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleConsume(event.player, item, event)
        }
    }

    @EventHandler
    fun on(event: PlayerSkillPrepareCastEvent) {
        val item = event.item ?: return
        val nekoStack = item.tryNekoStack ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleSkillPrepareCast(event.caster, item, event.skill, event)
        }
    }
}

/**
 * 无法被分类到 [ItemChangeListener] 和 [ItemBehaviorListener] 的监听逻辑.
 * 最终这些都应该合并到 [ItemChangeListener] 或 [ItemBehaviorListener] 中去.
 */
internal class ItemMiscellaneousListener : KoinComponent, Listener {
    private val attackSpeedEventHandler: AttackSpeedEventHandler by inject()
    private val skillEventHandler: SkillEventHandler by inject()

    //<editor-fold desc="Attack Speed">
    @EventHandler
    fun on(event: EntityDamageByEntityEvent) {
        val damager = event.damager as? Player ?: return
        val item = damager.inventory.itemInMainHand.takeUnlessEmpty() ?: return
        attackSpeedEventHandler.handlePlayerAttackEntity(damager, item, event)
    }

    @EventHandler
    fun on(event: EntityShootBowEvent) {
        val shooter = event.entity as? Player ?: return
        val item = event.bow ?: return
        attackSpeedEventHandler.handlePlayerShootBow(shooter, item, event)
    }
    //</editor-fold>

    //<editor-fold desc="Skills">
    @EventHandler
    fun on(event: PlayerInteractEvent) {
        val slot = event.hand ?: return
        val player = event.player
        val item = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return
        val nekoStack = item.tryNekoStack ?: return
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
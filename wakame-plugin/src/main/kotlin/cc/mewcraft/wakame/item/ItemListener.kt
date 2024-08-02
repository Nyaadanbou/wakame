package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.attribute.AttributeEventHandler
import cc.mewcraft.wakame.event.PlayerItemSlotChangeEvent
import cc.mewcraft.wakame.event.PlayerSkillPrepareCastEvent
import cc.mewcraft.wakame.kizami.KizamiEventHandler
import cc.mewcraft.wakame.skill.SkillEventHandler
import cc.mewcraft.wakame.util.takeUnlessEmpty
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MultipleItemListener : KoinComponent, Listener {
    private val attributeEventHandler: AttributeEventHandler by inject()
    private val kizamiEventHandler: KizamiEventHandler by inject()
    private val skillEventHandler: SkillEventHandler by inject()

    @EventHandler
    fun onItemSlotChange(event: PlayerItemSlotChangeEvent) {
        val player = event.player
        val itemSlot = event.slot
        val oldItem = event.oldItemStack
        val newItem = event.newItemStack

        attributeEventHandler.handlePlayerSlotChange(player, itemSlot, oldItem, newItem)
        kizamiEventHandler.handlePlayerSlotChange(player, itemSlot, oldItem, newItem)
        skillEventHandler.handlePlayerSlotChange(player, itemSlot, oldItem, newItem)
    }
}

/**
 * 此监听器包含的事件仅涉及到一个物品.
 */
class SingleItemListener : KoinComponent, Listener {
    private val skillEventHandler: SkillEventHandler by inject()

    /* Item Behavior Event Handlers */

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

    /* Skill Event Handlers */

    @EventHandler
    fun onClick(event: PlayerInteractEvent) {
        val player = event.player
        val slot = event.hand ?: return
        val item: ItemStack? = player.inventory.itemInMainHand.takeUnlessEmpty()
        val nekoStack = item?.tryNekoStack ?: return
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
    fun onAttack(event: EntityDamageByEntityEvent) {
        val damager = event.damager as? Player ?: return
        val entity = event.entity as? LivingEntity ?: return
        val item = damager.inventory.itemInMainHand.takeIfNekoStack()

        skillEventHandler.onAttack(damager, entity, item, event)
    }

    @EventHandler
    fun onProjectileHit(event: ProjectileHitEvent) {
        skillEventHandler.onProjectileHit(event.entity, event.hitEntity)
    }
}
package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.event.PlayerInventorySlotChangeEvent
import cc.mewcraft.wakame.event.PlayerSkillPrepareCastEvent
import cc.mewcraft.wakame.item.binary.PlayNekoStackFactory
import cc.mewcraft.wakame.skill.CasterAdapter
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.*

class ItemListener : Listener {
    @EventHandler
    fun onItemInteract(event: PlayerInteractEvent) {
        val item = event.item ?: return
        val nekoStack = PlayNekoStackFactory.maybe(item) ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleInteract(event.player, item, event.action, event)
        }
    }

    @EventHandler
    fun onDamageEntity(event: EntityDamageByEntityEvent) {
        // TODO: 这是一个 POC，可能需要考虑背包内的所有 Neko 物品
        val damager = event.damager as? Player ?: return
        val item = damager.inventory.itemInMainHand
        val nekoStack = PlayNekoStackFactory.maybe(item) ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleAttackEntity(damager, item, event.entity, event)
        }
    }

    @EventHandler
    fun onItemBreakBlock(event: BlockBreakEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand.takeIf { !it.isEmpty } ?: return
        val nekoStack = PlayNekoStackFactory.maybe(item) ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleBreakBlock(player, item, event)
        }
    }

    @EventHandler
    fun onItemDamage(event: PlayerItemDamageEvent) {
        val item = event.item
        val nekoStack = PlayNekoStackFactory.maybe(item) ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleDamage(event.player, item, event)
        }
    }

    @EventHandler
    fun onItemBreak(event: PlayerItemBreakEvent) {
        val item = event.brokenItem
        val nekoStack = PlayNekoStackFactory.maybe(item) ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleBreak(event.player, item, event)
        }
    }

    @EventHandler
    fun onItemHeld(event: PlayerItemHeldEvent) {
        val player = event.player
        val previousSlot = event.previousSlot
        val newSlot = event.newSlot
        val oldItem = player.inventory.getItem(previousSlot)
        val newItem = player.inventory.getItem(newSlot)

        if (oldItem != null) {
            val oldNekoStack = PlayNekoStackFactory.maybe(oldItem)
            oldNekoStack?.behaviors?.forEach { behavior ->
                behavior.handleItemUnheld(player, oldItem, event)
            }
        }

        if (newItem != null) {
            val newNekoStack = PlayNekoStackFactory.maybe(newItem)
            newNekoStack?.behaviors?.forEach { behavior ->
                behavior.handleItemHeld(player, newItem, event)
            }
        }
    }

    @EventHandler
    fun onSlotChange(event: PlayerInventorySlotChangeEvent) {
        val player = event.player
        val oldItem = event.oldItemStack.takeIf { !it.isEmpty }
        val newItem = event.newItemStack.takeIf { !it.isEmpty }

        if (oldItem != null) {
            val oldNekoStack = PlayNekoStackFactory.maybe(oldItem)
            oldNekoStack?.behaviors?.forEach { behavior ->
                behavior.handleSlotChangeOld(player, oldItem, event)
            }
        }

        if (newItem != null) {
            val newNekoStack = PlayNekoStackFactory.maybe(newItem)
            newNekoStack?.behaviors?.forEach { behavior ->
                behavior.handleSlotChangeNew(player, newItem, event)
            }
        }
    }

    @EventHandler
    fun onItemConsume(event: PlayerItemConsumeEvent) {
        val item = event.item
        val nekoStack = PlayNekoStackFactory.maybe(item) ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleConsume(event.player, item, event)
        }
    }

    @EventHandler
    fun onSkillPrepareCast(event: PlayerSkillPrepareCastEvent) {
        val item = event.item
        val nekoStack = PlayNekoStackFactory.maybe(item) ?: return
        nekoStack.behaviors.forEach { behavior ->
            behavior.handleSkillPrepareCast(event.playerCaster, item, event.skill, event)
        }
    }
}
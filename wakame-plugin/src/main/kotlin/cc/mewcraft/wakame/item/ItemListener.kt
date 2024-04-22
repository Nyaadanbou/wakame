package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.attribute.AttributeEventHandler
import cc.mewcraft.wakame.event.PlayerInventorySlotChangeEvent
import cc.mewcraft.wakame.event.PlayerSkillPrepareCastEvent
import cc.mewcraft.wakame.item.binary.PlayNekoStackFactory
import cc.mewcraft.wakame.kizami.KizamiEventHandler
import cc.mewcraft.wakame.skill.SkillEventHandler
import cc.mewcraft.wakame.util.takeUnlessEmpty
import com.destroystokyo.paper.event.player.PlayerJumpEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * 此监听器包含的事件需要同时处理两个物品。
 *
 * 这里需要更详细的文档说明。
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

    @EventHandler
    fun onJump(event: PlayerJumpEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand.takeUnlessEmpty() ?: return

        skillEventHandler.onJump(player, item)
    }
}

/**
 * 此监听器包含的事件仅需要处理一个物品。
 */
class SingleItemListener : Listener {
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
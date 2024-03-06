package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.event.NekoPlayerSlotChangeEvent
import cc.mewcraft.wakame.item.binary.NekoItemStack
import cc.mewcraft.wakame.item.binary.NekoItemStackFactory
import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.Multimap
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Handles the update process of [AttributeMap].
 *
 * Specifically, it handles custom attributes from neko items:
 * - which should be reflected in [AttributeMap] in real-time.
 * - which must be applied to players as real vanilla attribute modifiers.
 */
class AttributeHandler : KoinComponent, Listener {

    private val playerAttributeAccessor: PlayerAttributeAccessor by inject()

    ////// Listeners //////

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        val player = e.player
        playerAttributeAccessor.removeAttributeMap(player)
    }

    @EventHandler(ignoreCancelled = true)
    fun onHoldItem(e: PlayerItemHeldEvent) {
        val player = e.player
        val oldItem = player.inventory.getItem(e.previousSlot)
        val newItem = player.inventory.getItem(e.newSlot)
        removeAttributeModifiers(oldItem, player)
        addAttributeModifiers(newItem, player)
    }

    @EventHandler
    fun onInventoryChange(e: NekoPlayerSlotChangeEvent) {
        val player = e.player
        val slot = e.slot
        val oldItem = e.oldItem
        val newItem = e.newItem
        if (shouldHandle(slot, player)) {
            removeAttributeModifiers(oldItem, player)
            addAttributeModifiers(newItem, player)
        }
    }

    ////// Private Func //////

    private val effectiveNmsActiveSlots: Set<Int> = IntOpenHashSet(
        intArrayOf(
            1, // Crafting grid 1 (top-left)
            5, // Player helmet
            6, // Player chestplate
            7, // Player leggings
            8, // Player boots
        )
    )

    private fun shouldHandle(slot: Int, player: Player): Boolean {
        return slot in effectiveNmsActiveSlots || slot == (player.inventory.heldItemSlot + 36) /* player held item */
    }

    private fun getAttributeModifiers(bukkitItem: ItemStack?): Multimap<out Attribute, AttributeModifier> {
        if (bukkitItem == null || bukkitItem.isEmpty) {
            throw IllegalArgumentException("ItemStack must not be null, empty, or it has no item meta")
        }

        if (!bukkitItem.hasItemMeta()) {
            return ImmutableMultimap.of()
        }

        val nekoStack = bukkitItem.toNekoStack() ?: return ImmutableMultimap.of()
        return nekoStack.cells.getModifiers()
    }

    private fun addAttributeModifiers(bukkitItem: ItemStack?, player: Player) {
        if (bukkitItem == null || bukkitItem.isEmpty) {
            return
        }

        val attributeModifiers = getAttributeModifiers(bukkitItem)
        val attributeMap = playerAttributeAccessor.getAttributeMap(player)
        attributeMap.addAttributeModifiers(attributeModifiers)
    }

    private fun removeAttributeModifiers(bukkitItem: ItemStack?, player: Player) {
        if (bukkitItem == null || bukkitItem.isEmpty) {
            return
        }

        val attributeMap = playerAttributeAccessor.getAttributeMap(player)
        val nekoStack = bukkitItem.toNekoStack() ?: return
        attributeMap.clearModifiers(nekoStack.uuid)
    }

    private fun ItemStack?.toNekoStack(): NekoItemStack? {
        if (this == null || this.isEmpty) {
            return null
        }

        val nekoStack = NekoItemStackFactory.wrap(this)
        if (nekoStack.isNotNeko) {
            return null
        }

        return nekoStack
    }
}

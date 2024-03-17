package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.event.PlayerInventorySlotChangeEvent
import cc.mewcraft.wakame.item.binary.NekoItemStack
import cc.mewcraft.wakame.item.binary.NekoItemStackFactory
import cc.mewcraft.wakame.user.asNeko
import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.Multimap
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType.SlotType
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent

/**
 * Handles the update process of [AttributeMap].
 *
 * Specifically, it handles custom attributes from neko items, which
 * - should be updated in [AttributeMap] in real-time.
 * - must be applied to players as vanilla attribute modifiers.
 */
class AttributeEventHandler : KoinComponent {

    ////// Listeners //////

    /**
     * Updates attributes when the player switches their held item.
     */
    fun handlePlayerItemHeld(e: PlayerItemHeldEvent) {
        val player = e.player
        val oldItem = player.inventory.getItem(e.previousSlot)
        val newItem = player.inventory.getItem(e.newSlot)
        oldItem.removeAttributeModifiers(player)
        newItem.addAttributeModifiers(player)
    }

    /**
     * Updates attributes when an ItemStack is changed in the player's inventory.
     */
    fun handlePlayerInventorySlotChange(e: PlayerInventorySlotChangeEvent) {
        val player = e.player
        val slot = e.slot
        val rawSlot = e.rawSlot
        val oldItem = e.oldItemStack
        val newItem = e.newItemStack
        if (shouldHandle(slot, rawSlot, player)) {
            oldItem.removeAttributeModifiers(player)
            newItem.addAttributeModifiers(player)
        }
    }

    ////// Private Func //////

    private fun shouldHandle(slot: Int, rawSlot: Int, player: Player): Boolean {
        return (slot == (player.inventory.heldItemSlot)) || (player.openInventory.getSlotType(rawSlot) == SlotType.ARMOR)
    }

    /**
     * Gets attribute modifiers on the ItemStack.
     */
    private val ItemStack?.nekoAttributeModifiers: Multimap<Attribute, AttributeModifier>
        get() {
            if (this == null || this.isEmpty) {
                throw IllegalArgumentException("ItemStack must not be null, empty, or it has no item meta")
            }

            if (!this.hasItemMeta()) {
                return ImmutableMultimap.of()
            }

            val nekoStack = this.toNekoStack() ?: return ImmutableMultimap.of()
            return nekoStack.cells.getModifiers()
        }

    /**
     * Add the attribute modifiers of [this] for the [player].
     *
     * @param player the player we add attribute modifiers to
     */
    private fun ItemStack?.addAttributeModifiers(player: Player) {
        if (this == null || this.isEmpty) {
            return
        }

        val attributeModifiers = this.nekoAttributeModifiers
        val attributeMap = player.asNeko().attributeMap
        attributeMap.addAttributeModifiers(attributeModifiers)
    }

    /**
     * Remove the attribute modifiers of [this] for the [player].
     *
     * @param player the player we remove attribute modifiers from
     */
    private fun ItemStack?.removeAttributeModifiers(player: Player) {
        if (this == null || this.isEmpty) {
            return
        }

        // To remove an attribute modifier, we only need to know the UUID of it
        // and by design, the UUID of an attribute modifier is the UUID of the item
        // that provides the attribute modifier. Thus, we only need to get the UUID
        // of the item to clear the attribute modifier.
        val attributeMap = player.asNeko().attributeMap
        val nekoStack = this.toNekoStack() ?: return
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

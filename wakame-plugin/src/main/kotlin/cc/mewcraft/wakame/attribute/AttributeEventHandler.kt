package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.event.PlayerInventorySlotChangeEvent
import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.item.binary.NekoStackFactory
import cc.mewcraft.wakame.user.asNekoUser
import org.bukkit.entity.Player
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
        val previousSlot = e.previousSlot
        val newSlot = e.newSlot
        val oldItem = player.inventory.getItem(previousSlot)
        val newItem = player.inventory.getItem(newSlot)

        oldItem.removeAttributeModifiers(player) { effectiveSlot.testItemHeld(player, previousSlot, newSlot) }
        newItem.addAttributeModifiers(player) { effectiveSlot.testItemHeld(player, previousSlot, newSlot) }
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

        oldItem.removeAttributeModifiers(player) { effectiveSlot.testInventorySlotChange(player, slot, rawSlot) }
        newItem.addAttributeModifiers(player) { effectiveSlot.testInventorySlotChange(player, slot, rawSlot) }
    }

    ////// Private Func //////

    /**
     * Wrap the ItemStack as a NekoStack.
     */
    private fun ItemStack.asNekoStack(): NekoStack? {
        val nekoStack = NekoStackFactory.wrap(this)
        if (nekoStack.isNotNeko) {
            return null
        }

        return nekoStack
    }

    /**
     * Add the attribute modifiers of [this] for the [player].
     *
     * @param player the player we add attribute modifiers to
     */
    private inline fun ItemStack?.addAttributeModifiers(player: Player, testSlot: NekoStack.() -> Boolean) {
        if (this == null || this.isEmpty) {
            return
        }

        val attributeMap = player.asNekoUser().attributeMap
        if (!this.hasItemMeta()) {
            return
        }

        val nekoStack = this.asNekoStack() ?: return

        if (!nekoStack.testSlot()) {
            return
        }

        val attributeModifiers = nekoStack.cell.getAttributeModifiers()
        attributeMap.addAttributeModifiers(attributeModifiers)
    }

    /**
     * Remove the attribute modifiers of [this] for the [player].
     *
     * @param player the player we remove attribute modifiers from
     */
    private inline fun ItemStack?.removeAttributeModifiers(player: Player, testSlot: NekoStack.() -> Boolean) {
        if (this == null || this.isEmpty) {
            return
        }

        // To remove an attribute modifier, we only need to know the UUID of it
        // and by design, the UUID of an attribute modifier is the UUID of the item
        // that provides the attribute modifier. Thus, we only need to get the UUID
        // of the item to clear the attribute modifier.
        val nekoStack = this.asNekoStack() ?: return

        if (!nekoStack.testSlot()) {
            return
        }

        val attributeMap = player.asNekoUser().attributeMap
        attributeMap.clearModifiers(nekoStack.uuid)
    }
}

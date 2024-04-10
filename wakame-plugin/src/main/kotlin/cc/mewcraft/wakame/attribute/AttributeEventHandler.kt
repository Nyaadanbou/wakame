package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.item.binary.PlayNekoStack
import cc.mewcraft.wakame.item.binary.PlayNekoStackFactory
import cc.mewcraft.wakame.item.hasBehavior
import cc.mewcraft.wakame.item.schema.behavior.AttributeProvider
import cc.mewcraft.wakame.user.toUser
import org.bukkit.entity.Player
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

    /**
     * Updates attributes when the player switches their held item.
     *
     * @param player
     * @param previousSlot
     * @param newSlot
     * @param oldItem the old item to check with, or `null` if it's empty
     * @param newItem the new item to check with, or `null` if it's empty
     */
    fun handlePlayerItemHeld(
        player: Player,
        previousSlot: Int,
        newSlot: Int,
        oldItem: ItemStack?,
        newItem: ItemStack?,
    ) {
        updateAttributeModifiers(player, oldItem, newItem) {
            this.effectiveSlot.testItemHeld(player, previousSlot, newSlot) && this.hasBehavior<AttributeProvider>()
        }
    }

    /**
     * Updates attributes when an ItemStack is changed in the player's inventory.
     *
     * @param player
     * @param rawSlot
     * @param slot
     * @param oldItem the old item to check with, or `null` if it's empty
     * @param newItem the new item to check with, or `null` if it's empty
     */
    fun handlePlayerInventorySlotChange(
        player: Player,
        rawSlot: Int,
        slot: Int,
        oldItem: ItemStack?,
        newItem: ItemStack?,
    ) {
        updateAttributeModifiers(player, oldItem, newItem) {
            this.effectiveSlot.testInventorySlotChange(player, slot, rawSlot) && this.hasBehavior<AttributeProvider>()
        }
    }

    /**
     * Updates attribute modifiers.
     *
     * @param player
     * @param oldItem the old item to check with, or `null` if it's empty
     * @param newItem the new item to check with, or `null` if it's empty
     * @param predicate a function to test whether the item can provide attribute modifiers
     */
    private fun updateAttributeModifiers(
        player: Player,
        oldItem: ItemStack?,
        newItem: ItemStack?,
        predicate: PlayNekoStack.() -> Boolean, // TODO create an functional interface for this function type
    ) {
        oldItem?.removeAttributeModifiers(player, predicate)
        newItem?.addAttributeModifiers(player, predicate)
    }

    /**
     * Add the attribute modifiers of [this] for the [player].
     *
     * @param player the player we add attribute modifiers to
     * @param predicate
     * @receiver the ItemStack which may provide attribute modifiers
     */
    private inline fun ItemStack.addAttributeModifiers(player: Player, predicate: PlayNekoStack.() -> Boolean) {
        if (!this.hasItemMeta()) {
            return
        }

        val nekoStack = PlayNekoStackFactory.maybe(this) ?: return

        if (!nekoStack.predicate()) {
            return
        }

        val attributeMap = player.toUser().attributeMap
        val attributeModifiers = nekoStack.cell.getAttributeModifiers()
        attributeMap.addAttributeModifiers(attributeModifiers)
    }

    /**
     * Remove the attribute modifiers of [this] for the [player].
     *
     * @param player the player we remove attribute modifiers from
     * @param predicate
     * @receiver the ItemStack which may provide attribute modifiers
     */
    private inline fun ItemStack.removeAttributeModifiers(player: Player, predicate: PlayNekoStack.() -> Boolean) {
        // To remove an attribute modifier, we only need to know the UUID of it
        // and by design, the UUID of an attribute modifier is the UUID of the item
        // that provides the attribute modifier. Thus, we only need to get the UUID
        // of the item to clear the attribute modifier.

        val nekoStack = PlayNekoStackFactory.maybe(this) ?: return

        if (!nekoStack.predicate()) {
            return
        }

        val attributeMap = player.toUser().attributeMap
        attributeMap.clearModifiers(nekoStack.uuid)
    }
}

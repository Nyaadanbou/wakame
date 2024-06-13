package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.item.binary.PlayNekoStack
import cc.mewcraft.wakame.item.binary.PlayNekoStackPredicate
import cc.mewcraft.wakame.item.binary.tryNekoStack
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
     * Handles the attribute update when the player switches their held item.
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
            this.slot.testItemHeldEvent(player, previousSlot, newSlot) &&
            this.hasBehavior<AttributeProvider>()
        }
    }

    /**
     * Handles the attribute update when an ItemStack is changed in the player's inventory.
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
            this.slot.testInventorySlotChangeEvent(player, slot, rawSlot) &&
            this.hasBehavior<AttributeProvider>()
        }
    }

    /**
     * Updates attribute modifiers for the player.
     *
     * @param player
     * @param oldItem the old item to check with, or `null` if it's empty
     * @param newItem the new item to check with, or `null` if it's empty
     * @param predicate a function to test whether the item can provide attribute modifiers
     */
    private inline fun updateAttributeModifiers(
        player: Player,
        oldItem: ItemStack?,
        newItem: ItemStack?,
        predicate: PlayNekoStackPredicate,
    ) {
        oldItem?.tryNekoStack?.removeAttributeModifiers(player, predicate)
        newItem?.tryNekoStack?.addAttributeModifiers(player, predicate)
    }

    /**
     * Adds the attribute modifiers of [this] to the [player].
     *
     * @param player the player we add attribute modifiers to
     * @param predicate
     * @receiver the ItemStack which may provide attribute modifiers
     */
    private inline fun PlayNekoStack.addAttributeModifiers(player: Player, predicate: PlayNekoStackPredicate) {
        if (!this.predicate()) {
            return
        }

        val userAttributes = player.toUser().attributeMap
        val itemAttributes = this.cell.getAttributeModifiers()
        itemAttributes.forEach { attribute, modifier -> userAttributes[attribute]?.addModifier(modifier) }
    }

    /**
     * Removes the attribute modifiers of [this] from the [player].
     *
     * @param player the player we remove attribute modifiers from
     * @param predicate
     * @receiver the ItemStack which may provide attribute modifiers
     */
    private inline fun PlayNekoStack.removeAttributeModifiers(player: Player, predicate: PlayNekoStackPredicate) {
        if (!this.predicate()) {
            return
        }

        val userAttributes = player.toUser().attributeMap
        val itemAttributes = this.cell.getAttributeModifiers()
        itemAttributes.forEach { attribute, modifier -> userAttributes[attribute]?.removeModifier(modifier) }
    }
}

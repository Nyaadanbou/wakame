package cc.mewcraft.wakame.item.schema.behavior

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.event.PlayerInventorySlotChangeEvent
import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.item.binary.NekoStackFactory
import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.item.schema.meta.SchemaItemMeta
import cc.mewcraft.wakame.user.asNekoUser
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

interface AttributeProvider : ItemBehavior {
    companion object Factory : ItemBehaviorFactory<AttributeProvider> {
        override fun create(item: NekoItem, behaviorConfig: ConfigProvider): AttributeProvider {
            return Default()
        }
    }

    private class Default : AttributeProvider {
        override val requiredMetaTypes: Array<KClass<out SchemaItemMeta<*>>> = emptyArray()

        override fun handleItemHeld(player: Player, itemStack: ItemStack, event: PlayerItemHeldEvent) {
            val previousSlot = event.previousSlot
            val newSlot = event.newSlot
            itemStack.addAttributeModifiers(player) { effectiveSlot.testItemHeld(player, previousSlot, newSlot) }
        }

        override fun handleItemUnHeld(player: Player, itemStack: ItemStack, event: PlayerItemHeldEvent) {
            val previousSlot = event.previousSlot
            val newSlot = event.newSlot
            itemStack.removeAttributeModifiers(player) { effectiveSlot.testItemHeld(player, previousSlot, newSlot) }
        }

        override fun handleSlotChangeNew(player: Player, itemStack: ItemStack, event: PlayerInventorySlotChangeEvent) {
            val slot = event.slot
            val rawSlot = event.rawSlot
            itemStack.addAttributeModifiers(player) { effectiveSlot.testInventorySlotChange(player, slot, rawSlot) }
        }

        override fun handleSlotChangeOld(player: Player, itemStack: ItemStack, event: PlayerInventorySlotChangeEvent) {
            val slot = event.slot
            val rawSlot = event.rawSlot
            itemStack.removeAttributeModifiers(player) { effectiveSlot.testInventorySlotChange(player, slot, rawSlot) }
        }

        ////// Private Func //////

        /**
         * Add the attribute modifiers of [this] for the [player].
         *
         * @param player the player we add attribute modifiers to
         */
        private inline fun ItemStack.addAttributeModifiers(player: Player, testSlot: NekoStack.() -> Boolean) {
            val nekoStack = NekoStackFactory.wrap(this)

            if (!nekoStack.testSlot()) {
                return
            }

            val attributeMap = player.asNekoUser().attributeMap
            val attributeModifiers = nekoStack.cell.getAttributeModifiers()
            attributeMap.addAttributeModifiers(attributeModifiers)
        }

        /**
         * Remove the attribute modifiers of [this] for the [player].
         *
         * @param player the player we remove attribute modifiers from
         */
        private inline fun ItemStack.removeAttributeModifiers(player: Player, testSlot: NekoStack.() -> Boolean) {
            // To remove an attribute modifier, we only need to know the UUID of it
            // and by design, the UUID of an attribute modifier is the UUID of the item
            // that provides the attribute modifier. Thus, we only need to get the UUID
            // of the item to clear the attribute modifier.
            val nekoStack = NekoStackFactory.wrap(this)

            if (!nekoStack.testSlot()) {
                return
            }

            val attributeMap = player.asNekoUser().attributeMap
            attributeMap.clearModifiers(nekoStack.uuid)
        }
    }
}
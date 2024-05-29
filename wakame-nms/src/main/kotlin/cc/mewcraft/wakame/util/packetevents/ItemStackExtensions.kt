package cc.mewcraft.wakame.util.packetevents

import com.github.retrooper.packetevents.protocol.component.ComponentTypes
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemLore
import com.github.retrooper.packetevents.protocol.item.ItemStack
import net.kyori.adventure.text.Component
import kotlin.jvm.optionals.getOrNull

fun ItemStack.takeUnlessEmpty(): ItemStack? {
    return this.takeIf { !it.isEmpty } // FIXME 需要这个吗？
}

/**
 * Sets the custom name. You may pass a `null` to remove the name.
 */
var ItemStack.backingCustomName: Component?
    get() {
        return this.getComponent(ComponentTypes.CUSTOM_NAME).getOrNull()
    }
    set(value) {
        if (value != null) {
            this.setComponent(ComponentTypes.CUSTOM_NAME, value)
        } else {
            this.unsetComponent(ComponentTypes.CUSTOM_NAME)
        }
    }

/**
 * Sets the item name. You may pass a `null` to remove it.
 */
var ItemStack.backingItemName: Component?
    get() {
        return this.getComponent(ComponentTypes.ITEM_NAME).getOrNull()
    }
    set(value) {
        if (value != null) {
            this.setComponent(ComponentTypes.ITEM_NAME, value)
        } else {
            this.unsetComponent(ComponentTypes.ITEM_NAME)
        }
    }

/**
 * Sets the item lore. You may pass a `null` to remove it.
 */
var ItemStack.backingLore: List<Component>?
    get() {
        return this.getComponent(ComponentTypes.LORE).getOrNull()?.lines
    }
    set(value) {
        if (value != null) {
            this.setComponent(ComponentTypes.LORE, ItemLore(value))
        } else {
            this.unsetComponent(ComponentTypes.LORE)
        }
    }

/**
 * Sets the custom model data. You may pass a `null` to remove it.
 */
var ItemStack.backingCustomModelData: Int?
    get() {
        return this.getComponent(ComponentTypes.CUSTOM_MODEL_DATA).getOrNull()
    }
    set(value) {
        if (value != null) {
            this.setComponent(ComponentTypes.CUSTOM_MODEL_DATA, value)
        } else {
            this.unsetComponent(ComponentTypes.CUSTOM_MODEL_DATA)
        }
    }
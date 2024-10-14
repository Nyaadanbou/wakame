package cc.mewcraft.wakame.util

import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import net.minecraft.world.item.ItemStack as MojangStack

/**
 * Checks if `this` [ItemStack] has a backing NMS object.
 */
val ItemStack.isNms: Boolean
    get() {
        return if (this is CraftItemStack) {
            this.handle !== null
        } else {
            false
        }
    }

fun <T> MojangStack.modify(type: DataComponentType<T>, block: (T) -> T) {
    val oldData = get(type) ?: return
    val newData = block(oldData)
    set(type, newData)
}

fun ItemStack.showAttributeModifiers(value: Boolean) {
    handle?.modify(DataComponents.ATTRIBUTE_MODIFIERS) { data -> data.withTooltip(value) }
}

fun ItemStack.showCanBreak(value: Boolean) {
    handle?.modify(DataComponents.CAN_BREAK) { data -> data.withTooltip(value) }
}

fun ItemStack.showCanPlaceOn(value: Boolean) {
    handle?.modify(DataComponents.CAN_PLACE_ON) { data -> data.withTooltip(value) }
}

fun ItemStack.showDyedColor(value: Boolean) {
    handle?.modify(DataComponents.DYED_COLOR) { data -> data.withTooltip(value) }
}

fun ItemStack.showEnchantments(value: Boolean) {
    handle?.modify(DataComponents.ENCHANTMENTS) { data -> data.withTooltip(value) }
}

fun ItemStack.showJukeboxPlayable(value: Boolean) {
    handle?.modify(DataComponents.JUKEBOX_PLAYABLE) { data -> data.withTooltip(value) }
}

fun ItemStack.showStoredEnchantments(value: Boolean) {
    handle?.modify(DataComponents.STORED_ENCHANTMENTS) { data -> data.withTooltip(value) }
}

fun ItemStack.showTrim(value: Boolean) {
    handle?.modify(DataComponents.TRIM) { data -> data.withTooltip(value) }
}

fun ItemStack.showUnbreakable(value: Boolean) {
    handle?.modify(DataComponents.UNBREAKABLE) { data -> data.withTooltip(value) }
}
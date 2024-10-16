package cc.mewcraft.wakame.util

import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.ItemAttributeModifiers
import net.minecraft.world.item.enchantment.ItemEnchantments
import org.bukkit.inventory.ItemStack
import net.minecraft.world.item.ItemStack as MojangStack

private fun <T> MojangStack.modify(type: DataComponentType<T>, block: (T) -> T) {
    val oldData = get(type) ?: return
    val newData = block(oldData)
    set(type, newData)
}

fun ItemStack.showAttributeModifiers(value: Boolean) {
    val handle = this.handle ?: return
    val data = handle.get(DataComponents.ATTRIBUTE_MODIFIERS) ?: return
    if (data === ItemAttributeModifiers.EMPTY || data.modifiers.isEmpty()) {
        return
    }
    handle.modify(DataComponents.ATTRIBUTE_MODIFIERS) { data1 -> data1.withTooltip(value) }
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
    val handle = this.handle ?: return
    val data = handle.get(DataComponents.ENCHANTMENTS) ?: return
    if (data === ItemEnchantments.EMPTY || data.isEmpty) {
        return
    }
    handle.modify(DataComponents.ENCHANTMENTS) { data1 -> data1.withTooltip(value) }
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
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

class ItemStackDSL(
    internal val target: ItemStack,
) {
    var itemName
        get() =
            target.itemName0
        set(value) {
            target.itemName0 = value
        }

    var customName
        get() =
            target.customName0
        set(value) {
            target.customName0 = value
        }

    var lore
        get() =
            target.lore0
        set(value) {
            target.lore0 = value
        }

    var customModelData
        get() =
            target.customModelData0
        set(value) {
            target.customModelData0 = value
        }

    fun showAttributeModifiers(value: Boolean) {
        target.showAttributeModifiers(value)
    }

    fun showCanBreak(value: Boolean) {
        target.showCanBreak(value)
    }

    fun showCanPlaceOn(value: Boolean) {
        target.showCanPlaceOn(value)
    }

    fun showDyedColor(value: Boolean) {
        target.showDyedColor(value)
    }

    fun showEnchantments(value: Boolean) {
        target.showEnchantments(value)
    }

    fun showJukeboxPlayable(value: Boolean) {
        target.showJukeboxPlayable(value)
    }

    fun showStoredEnchantments(value: Boolean) {
        target.showStoredEnchantments(value)
    }

    fun showTrim(value: Boolean) {
        target.showTrim(value)
    }

    fun showUnbreakable(value: Boolean) {
        target.showUnbreakable(value)
    }

    fun showNothing() {
        showAttributeModifiers(false)
        showCanBreak(false)
        showCanPlaceOn(false)
        showDyedColor(false)
        showEnchantments(false)
        showJukeboxPlayable(false)
        showStoredEnchantments(false)
        showTrim(false)
        showUnbreakable(false)
    }
}

fun ItemStack.edit(block: ItemStackDSL.() -> Unit): ItemStack {
    return ItemStackDSL(this).apply(block).target
}

private val EMPTY_ATTRIBUTE_MODIFIERS = ItemAttributeModifiers.EMPTY.withTooltip(false)

// FIXME 等 Mojang 修复: https://bugs.mojang.com/browse/MC-271826.
//  目前我们无法通过正常的途径 `!attribute_modifiers` 移除盔甲上的默认属性.
//  暂时一刀切, 无论什么情况都改为空属性修饰符+不显示, 等 Mojang 修复后再优化.
fun ItemStack.showAttributeModifiers(value: Boolean) {
    val handle = this.handle ?: return

    /* val data = handle.get(DataComponents.ATTRIBUTE_MODIFIERS) ?: return
    // 对于所有盔甲物品, 即使它们有属性修饰符的加成, 但这里获取到的依然是 `EMPTY` (MC-271826).
    // 也就是说, 我们无法准确的通过 ItemAttributeModifiers 来判断一个盔甲物品是否有属性修饰符.
    if ((data === ItemAttributeModifiers.EMPTY || data.modifiers.isEmpty()) && handle.item !is ArmorItem) {
        return
    } */

    if (value) {
        handle.modify(DataComponents.ATTRIBUTE_MODIFIERS) { data -> data.withTooltip(true) }
    } else {
        handle.set(DataComponents.ATTRIBUTE_MODIFIERS, EMPTY_ATTRIBUTE_MODIFIERS)
    }
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
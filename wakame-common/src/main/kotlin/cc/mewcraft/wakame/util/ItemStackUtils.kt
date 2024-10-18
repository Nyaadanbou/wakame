package cc.mewcraft.wakame.util

import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun ItemStack.takeUnlessEmpty(): ItemStack? =
    if (isEmpty) null else this

fun ItemStack?.isEmpty(): Boolean =
    this == null || isEmpty

inline fun <reified T : ItemMeta> ItemStack.editMeta(crossinline block: (T) -> Unit) {
    this.editMeta(T::class.java) { block(it) }
}

fun ItemStack.hideTooltip(hide: Boolean): ItemStack {
    editMeta { it.isHideTooltip = hide }
    return this
}

fun ItemMeta.hideTooltip(hide: Boolean): ItemMeta {
    isHideTooltip = hide
    return this
}

fun ItemStack.hideAllFlags(): ItemStack {
    editMeta { it.addItemFlags(*ItemFlag.entries.toTypedArray()) }
    return this
}

fun ItemMeta.hideAllFlags(): ItemMeta {
    addItemFlags(*ItemFlag.entries.toTypedArray())
    return this
}
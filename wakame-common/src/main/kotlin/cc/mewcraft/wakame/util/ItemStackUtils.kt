package cc.mewcraft.wakame.util

import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun ItemStack.takeUnlessEmpty(): ItemStack? =
    if (type.isAir || amount <= 0) null else this

fun ItemStack?.isEmpty(): Boolean =
    this == null || type.isAir || amount <= 0

inline fun <reified T : ItemMeta> ItemStack.editMeta(crossinline block: (T) -> Unit) {
    this.editMeta(T::class.java) { block(it) }
}

fun ItemStack.hideTooltip(hide: Boolean): ItemStack {
    editMeta { it.isHideTooltip = hide }
    return this
}

fun ItemStack.hideAllFlags(): ItemStack {
    editMeta { it.addItemFlags(*ItemFlag.entries.toTypedArray()) }
    return this
}
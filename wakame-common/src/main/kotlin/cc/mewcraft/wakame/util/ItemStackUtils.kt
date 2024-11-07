package cc.mewcraft.wakame.util

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun ItemStack.takeUnlessEmpty(): ItemStack? =
    if (isEmpty) null else this

fun ItemStack?.isEmpty(): Boolean =
    this == null || isEmpty

inline fun <reified T : ItemMeta> ItemStack.editMeta(crossinline block: (T) -> Unit) {
    this.editMeta(T::class.java) { block(it) }
}

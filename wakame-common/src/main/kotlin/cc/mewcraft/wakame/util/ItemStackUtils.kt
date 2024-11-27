package cc.mewcraft.wakame.util

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

fun ItemStack.takeUnlessEmpty(): ItemStack? =
    if (isEmpty) null else this

@OptIn(ExperimentalContracts::class)
fun ItemStack?.isEmpty(): Boolean {
    contract { returns(false) implies (this@isEmpty != null) }
    return this == null || isEmpty
}

inline fun <reified T : ItemMeta> ItemStack.editMeta(crossinline block: (T) -> Unit) {
    this.editMeta(T::class.java) { block(it) }
}

package cc.mewcraft.wakame.util

import org.bukkit.inventory.ItemStack

fun ItemStack.takeUnlessEmpty(): ItemStack? =
    if (type.isAir || amount <= 0) null else this

fun ItemStack?.isEmpty(): Boolean =
    this == null || type.isAir || amount <= 0
package cc.mewcraft.wakame.util

import com.github.retrooper.packetevents.protocol.item.ItemStack

fun ItemStack.takeUnlessEmpty(): ItemStack? = this.takeIf { !it.isEmpty && it.nbt != null }
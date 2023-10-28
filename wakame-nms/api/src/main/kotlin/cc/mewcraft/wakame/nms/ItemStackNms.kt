package cc.mewcraft.wakame.nms

import net.kyori.adventure.nbt.CompoundBinaryTag
import org.bukkit.inventory.ItemStack

interface ItemStackNms {
    fun readNbt(item: ItemStack): CompoundBinaryTag?
    fun writeNbt(item: ItemStack, compound: CompoundBinaryTag): ItemStack

    companion object {
        lateinit var provider: ItemStackNms
    }
}
package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.nms.ItemStackNms
import net.kyori.adventure.nbt.CompoundBinaryTag
import org.bukkit.inventory.ItemStack

private val nms = ItemStackNms()

fun ItemStack.readNbt(): CompoundBinaryTag =
    nms.readNbt(this)

fun ItemStack.readNbtOrNull(): CompoundBinaryTag? =
    nms.readNbtOrNull(this)

fun ItemStack.modifyNbt(compound: CompoundBinaryTag.() -> CompoundBinaryTag) =
    nms.modifyNbt(this, compound)

fun ItemStack.copyWriteNbt(compound: CompoundBinaryTag.() -> CompoundBinaryTag): ItemStack =
    nms.copyWriteNbt(this, compound)

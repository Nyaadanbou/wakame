package cc.mewcraft.wakame.util

import cc.mewcraft.wakame.nms.ItemStackNms
import cc.mewcraft.wakame.nms.ItemStackNmsImp
import net.kyori.adventure.nbt.CompoundBinaryTag
import org.bukkit.inventory.ItemStack

private val imp = ItemStackNmsImp()

fun ItemStack.readNbt(): CompoundBinaryTag? =
    imp.readNbt(this)

fun ItemStack.writeNbt(compound: CompoundBinaryTag): ItemStack =
    imp.writeNbt(this, compound)

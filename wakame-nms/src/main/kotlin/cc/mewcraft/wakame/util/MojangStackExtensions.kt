package cc.mewcraft.wakame.util

import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData

internal fun ItemStack.getCustomData(): CompoundTag? {
    val customData = this.get(DataComponents.CUSTOM_DATA)

    @Suppress("DEPRECATION")
    val compoundTag = customData?.unsafe // this returns the backing CompoundTag for us
    return compoundTag
}

internal fun ItemStack.getCustomDataOrCreate(): CompoundTag {
    val customData = this.get(DataComponents.CUSTOM_DATA) ?: run {
        val empty = CustomData.of(CompoundTag())
        this.set(DataComponents.CUSTOM_DATA, empty)
        return@run empty
    }

    @Suppress("DEPRECATION")
    val compoundTag = customData.unsafe // this returns the backing CompoundTag for us
    return compoundTag
}

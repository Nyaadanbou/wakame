package cc.mewcraft.wakame.util

import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData

internal fun ItemStack.getCustomData(): CompoundTag? {
    val customData = this.get(DataComponents.CUSTOM_DATA)
    return customData?.copyTag()
}

internal fun ItemStack.getCustomDataOrCreate(): CompoundTag {
    val customData = this.get(DataComponents.CUSTOM_DATA)
    if (customData == null) {
        val newCustomData = CustomData.of(CompoundTag())
        this.set(DataComponents.CUSTOM_DATA, newCustomData)
        return newCustomData.copyTag()
    }
    return customData.copyTag()
}

internal fun ItemStack.setCustomData(tag: CompoundTag) {
    val customData = CustomData.of(tag)
    this.set(DataComponents.CUSTOM_DATA, customData)
}

@Deprecated("Breaking immutability", level = DeprecationLevel.WARNING)
internal fun ItemStack.getUnsafeCustomData(): CompoundTag? {
    val customData = this.get(DataComponents.CUSTOM_DATA)
    return customData?.unsafe
}

@Deprecated("Breaking immutability", level = DeprecationLevel.WARNING)
internal fun ItemStack.getUnsafeCustomDataOrCreate(): CompoundTag {
    val customData = this.get(DataComponents.CUSTOM_DATA)
    if (customData == null) {
        val newCustomData = CustomData.of(CompoundTag())
        this.set(DataComponents.CUSTOM_DATA, newCustomData)
        return newCustomData.unsafe
    }
    return customData.unsafe
}

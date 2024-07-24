package cc.mewcraft.wakame.util

import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData

internal fun ItemStack.setCustomData(tag: CompoundTag) {
    val customData = CustomData.of(tag)
    this.set(DataComponents.CUSTOM_DATA, customData)
}

internal fun ItemStack.getCustomData(): CompoundTag? {
    val customData = this.get(DataComponents.CUSTOM_DATA)
    return customData?.copyTag() // 返回一个副本
}

internal fun ItemStack.getDirectCustomData(): CompoundTag? {
    val customData = this.get(DataComponents.CUSTOM_DATA)
    return customData?.unsafe // 直接返回 backing CompoundTag
}

internal fun ItemStack.getDirectCustomDataOrCreate(): CompoundTag {
    val customData = this.get(DataComponents.CUSTOM_DATA)
    if (customData === null) {
        val empty = CustomData.of(CompoundTag())
        this.set(DataComponents.CUSTOM_DATA, empty)
        return empty.unsafe
    }
    return customData.unsafe // 直接返回 backing CompoundTag
}

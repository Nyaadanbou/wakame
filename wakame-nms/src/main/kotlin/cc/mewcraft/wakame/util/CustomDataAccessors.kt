package cc.mewcraft.wakame.util

import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData

internal fun ItemStack.getCustomData(): CompoundTag? {
    val data = this.get(DataComponents.CUSTOM_DATA)
    return data?.copyTag()
}

internal fun ItemStack.setCustomData(compoundTag: CompoundTag) {
    val data = CustomData.of(compoundTag)
    this.set(DataComponents.CUSTOM_DATA, data)
}

internal fun ItemStack.unsetCustomData() {
    this.remove(DataComponents.CUSTOM_DATA)
}

@Deprecated("Breaking immutability", level = DeprecationLevel.WARNING)
internal fun ItemStack.getUnsafeCustomData(): CompoundTag? {
    val data = this.get(DataComponents.CUSTOM_DATA)
    return data?.unsafe
}

@Deprecated("Breaking immutability", level = DeprecationLevel.WARNING)
internal fun ItemStack.setUnsafeCustomData(compoundTag: CompoundTag) {
    val data = this.get(DataComponents.CUSTOM_DATA)
    if (data != null) {
        val tag = data.unsafe
        tag.allKeys.forEach(tag::remove) // 清除所有的标签
        tag.merge(compoundTag)
    } else {
        this.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag))
    }
}

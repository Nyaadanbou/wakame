@file:Suppress("DeprecatedCallableAddReplaceWith")

package cc.mewcraft.wakame.util

import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData

internal fun ItemStack.getCustomData(): CompoundTag? {
    return get(DataComponents.CUSTOM_DATA)?.copyTag()
}

internal fun ItemStack.setCustomData(compoundTag: CompoundTag) {
    set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag))
}

internal fun ItemStack.unsetCustomData() {
    remove(DataComponents.CUSTOM_DATA)
}

@Deprecated("Breaking immutability!", level = DeprecationLevel.WARNING)
internal fun ItemStack.getUnsafeCustomData(): CompoundTag? {
    return get(DataComponents.CUSTOM_DATA)?.unsafe
}

@Deprecated("Breaking immutability!", level = DeprecationLevel.WARNING)
internal fun ItemStack.setUnsafeCustomData(compoundTag: CompoundTag) {
    val data = get(DataComponents.CUSTOM_DATA)
    if (data !== null) {
        data.unsafe.merge(compoundTag) // 采用 merge, 而非 remove all 再 put
    } else {
        set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag))
    }
}

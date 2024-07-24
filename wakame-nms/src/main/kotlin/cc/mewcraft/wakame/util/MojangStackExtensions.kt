package cc.mewcraft.wakame.util

import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.CustomData

internal val ItemStack.minecraftCustomData: CompoundTag?
    get() {
        val customData = this.get(DataComponents.CUSTOM_DATA)
        return customData?.unsafe // 直接返回 backing CompoundTag
    }

internal val ItemStack.minecraftCustomDataOrCreate: CompoundTag
    get() {
        val customData = this.get(DataComponents.CUSTOM_DATA) ?: run {
            val empty = CustomData.of(CompoundTag())
            this.set(DataComponents.CUSTOM_DATA, empty)
            return@run empty
        }
        return customData.unsafe // 直接返回 backing CompoundTag
    }

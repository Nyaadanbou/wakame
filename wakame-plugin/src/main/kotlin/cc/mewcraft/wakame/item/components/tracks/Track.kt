package cc.mewcraft.wakame.item.components.tracks

import cc.mewcraft.nbt.CompoundTag

/**
 * 可跟踪的信息.
 */
interface Track {

    fun saveNbt(): CompoundTag

}
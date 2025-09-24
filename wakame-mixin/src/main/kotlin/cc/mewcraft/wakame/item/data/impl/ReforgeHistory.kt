package cc.mewcraft.wakame.item.data.impl

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class ReforgeHistory(
    @Setting(nodeFromParent = true)
    val modCount: Int,
) {
    companion object {
        /**
         * 一个崭新的 [ReforgeHistory], 相当于从未重铸过.
         */
        @JvmStatic
        val ZERO = ReforgeHistory(0)
    }

    fun incCount(value: Int): ReforgeHistory {
        return copy(modCount = modCount + value)
    }

    fun decCount(value: Int): ReforgeHistory {
        return copy(modCount = modCount - value)
    }
}
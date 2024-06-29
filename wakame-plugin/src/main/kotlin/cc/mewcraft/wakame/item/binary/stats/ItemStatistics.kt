package cc.mewcraft.wakame.item.binary.stats

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.getOrPut

/**
 * 代表一个物品上的统计数据。
 */
sealed interface ItemStatistics {
    /**
     * The [ItemStatisticsAccessor] encompassing `this`.
     */
    val accessor: ItemStatisticsAccessor

    /**
     * The path to the tags of this [ItemStatistics].
     */
    val nbtPath: String

    /**
     * Gets the root tag or `null` if it does not exist.
     */
    val rootOrNull: CompoundTag?
        get() = accessor.rootOrNull?.getCompoundOrNull(nbtPath)

    /**
     * Gets the root tag or create it if it does not exist.
     */
    val rootOrCreate: CompoundTag
        get() = accessor.rootOrCreate.getOrPut(nbtPath, CompoundTag::create)
}
package cc.mewcraft.wakame.item.binary.stats

import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.getOrPut
import me.lucko.helper.shadows.nbt.CompoundShadowTag

/**
 * 代表一个物品上的统计数据。
 */
sealed interface ItemStatistics {
    /**
     * The [ItemStatisticsHolder] encompassing `this`.
     */
    val holder: ItemStatisticsHolder

    /**
     * The path to the tags of this [ItemStatistics].
     */
    val nbtPath: String

    /**
     * Gets the root tag or `null` if it does not exist.
     */
    val rootOrNull: CompoundShadowTag?
        get() = holder.rootOrNull?.getCompoundOrNull(nbtPath)

    /**
     * Gets the root tag or create it if it does not exist.
     */
    val rootOrCreate: CompoundShadowTag
        get() = holder.rootOrCreate.getOrPut(nbtPath, CompoundShadowTag::create)
}
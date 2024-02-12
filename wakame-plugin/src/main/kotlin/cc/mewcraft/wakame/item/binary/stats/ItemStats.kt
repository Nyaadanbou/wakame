package cc.mewcraft.wakame.item.binary.stats

import cc.mewcraft.wakame.annotation.InternalApi
import me.lucko.helper.shadows.nbt.CompoundShadowTag

/**
 * 代表一个物品上的统计数据。
 */
sealed interface ItemStats {
    /**
     * The [ItemStatsAccessor] encompassing `this`.
     */
    val accessor: ItemStatsAccessor

    /**
     * The path to the tags of this [ItemStats].
     */
    @InternalApi
    val nbtPath: String

    /**
     * Encompassing all tags of this [ItemStats].
     */
    @InternalApi
    val tags: CompoundShadowTag get() = accessor.tags.getCompound(nbtPath)
}
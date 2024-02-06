package cc.mewcraft.wakame.item.binary.stats

import me.lucko.helper.shadows.nbt.CompoundShadowTag

/**
 * 代表一个物品上的统计数据。
 */
sealed interface ItemStats {
    companion object {
        /**
         * The namespace of all types of [ItemStats].
         */
        const val NAMESPACE = "stats"
    }

    /**
     * The [ItemStatsAccessor] encompassing `this`.
     */
    val accessor: ItemStatsAccessor

    /**
     * The path to the tags of this [ItemStats].
     */
    val nbtPath: String

    /**
     * Encompassing all tags of this [ItemStats].
     */
    val tags: CompoundShadowTag get() = accessor.tags.getCompound(nbtPath)
}
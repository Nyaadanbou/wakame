package cc.mewcraft.wakame.item.binary.stats

import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.getOrPut
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

    @InternalApi
    val rootOrNull: CompoundShadowTag?
        get() = accessor.rootOrNull?.getCompoundOrNull(nbtPath)

    @InternalApi
    val rootOrCreate: CompoundShadowTag
        get() = accessor.rootOrCreate.getOrPut(nbtPath, CompoundShadowTag::create)
}
package cc.mewcraft.wakame.item.binary.stats

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.item.StatisticsBinaryKeys
import cc.mewcraft.wakame.item.binary.NekoStackBase
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.getOrPut

@JvmInline
internal value class ItemStatisticsAccessorImpl(
    private val base: NekoStackBase,
) : ItemStatisticsAccessor {
    override val rootOrNull: CompoundTag?
        get() = base.tags.getCompoundOrNull(StatisticsBinaryKeys.BASE)
    override val rootOrCreate: CompoundTag
        get() = base.tags.getOrPut(StatisticsBinaryKeys.BASE, CompoundTag::create)

    override val ENTITY_KILLS: EntityKillsStatistics
        get() = EntityKillsStatistics(this)
    override val PEAK_DAMAGE: PeakDamageStatistics
        get() = PeakDamageStatistics(this)
    override val REFORGE: ReforgeStatistics
        get() = ReforgeStatistics(this)
}
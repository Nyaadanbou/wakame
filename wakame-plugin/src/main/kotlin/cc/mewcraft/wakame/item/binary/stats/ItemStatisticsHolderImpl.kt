package cc.mewcraft.wakame.item.binary.stats

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.item.binary.NekoStackImpl
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.getOrPut
import me.lucko.helper.shadows.nbt.CompoundShadowTag

@JvmInline
internal value class ItemStatisticsHolderImpl(
    private val base: NekoStackImpl,
) : ItemStatisticsHolder {
    override val rootOrNull: CompoundShadowTag?
        get() = base.tags.getCompoundOrNull(NekoTags.Stats.ROOT)
    override val rootOrCreate: CompoundShadowTag
        get() = base.tags.getOrPut(NekoTags.Stats.ROOT, CompoundShadowTag::create)

    override val entityKills: EntityKillsStatistics
        get() = EntityKillsStatistics(this)
    override val peakDamage: PeakDamageStatistics
        get() = PeakDamageStatistics(this)
    override val reforge: ReforgeStatistics
        get() = ReforgeStatistics(this)
}
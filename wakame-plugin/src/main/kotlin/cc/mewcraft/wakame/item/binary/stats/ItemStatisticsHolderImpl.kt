package cc.mewcraft.wakame.item.binary.stats

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.item.binary.NekoStackImpl
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.getOrPut
import me.lucko.helper.shadows.nbt.CompoundShadowTag

internal class ItemStatisticsHolderImpl(
    private val base: NekoStackImpl,
) : ItemStatisticsHolder {
    @InternalApi
    override val rootOrNull: CompoundShadowTag?
        get() = base.tags.getCompoundOrNull(NekoTags.Stats.ROOT)

    @InternalApi
    override val rootOrCreate: CompoundShadowTag
        get() = base.tags.getOrPut(NekoTags.Stats.ROOT, CompoundShadowTag::create)

    override val entityKills: EntityKillsStatistics = EntityKillsStatistics(this)
    override val peakDamage: PeakDamageStatistics = PeakDamageStatistics(this)
    override val reforge: ReforgeStatistics = ReforgeStatistics(this)
}
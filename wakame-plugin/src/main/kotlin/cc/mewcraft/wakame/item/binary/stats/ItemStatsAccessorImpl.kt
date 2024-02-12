package cc.mewcraft.wakame.item.binary.stats

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.item.binary.WakaItemStackImpl
import cc.mewcraft.wakame.util.getOrPut
import me.lucko.helper.shadows.nbt.CompoundShadowTag

@OptIn(InternalApi::class)
internal class ItemStatsAccessorImpl(
    private val base: WakaItemStackImpl,
) : ItemStatsAccessor {

    override val tags: CompoundShadowTag
        get() = base.tags.getOrPut(NekoTags.Stats.ROOT, CompoundShadowTag::create)

    //<editor-fold desc="Item Stats Instances">
    override val entityKillsStats: EntityKillsStats = EntityKillsStats(this)
    override val peakDamageStats: PeakDamageStats = PeakDamageStats(this)
    override val reforgeStats: ReforgeStats = ReforgeStats(this)
    //</editor-fold>
}
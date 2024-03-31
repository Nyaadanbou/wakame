package cc.mewcraft.wakame.item.binary.stats

import cc.mewcraft.wakame.NekoTags
import cc.mewcraft.wakame.item.binary.NekoStackImpl
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.getOrPut
import me.lucko.helper.shadows.nbt.CompoundShadowTag

@JvmInline
internal value class ItemStatisticsAccessorImpl(
    private val base: NekoStackImpl,
) : ItemStatisticsAccessor {
    override val rootOrNull: CompoundShadowTag?
        get() = base.tags.getCompoundOrNull(NekoTags.Stats.ROOT)
    override val rootOrCreate: CompoundShadowTag
        get() = base.tags.getOrPut(NekoTags.Stats.ROOT, CompoundShadowTag::create)

    override val ENTITY_KILLS: EntityKillsStatistics
        get() = EntityKillsStatistics(this)
    override val PEAK_DAMAGE: PeakDamageStatistics
        get() = PeakDamageStatistics(this)
    override val REFORGE: ReforgeStatistics
        get() = ReforgeStatistics(this)
}
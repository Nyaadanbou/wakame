package cc.mewcraft.wakame.item.binary.stats

import cc.mewcraft.wakame.annotation.InternalApi
import me.lucko.helper.shadows.nbt.CompoundShadowTag

interface ItemStatsAccessor {
    @InternalApi
    val rootOrNull: CompoundShadowTag?

    @InternalApi
    val rootOrCreate: CompoundShadowTag

    ////// All Statistics Categories //////

    /**
     * 实体击杀数
     */
    val entityKills: EntityKillsStats

    /**
     * 单次最高伤害
     */
    val peakDamage: PeakDamageStats

    /**
     * 重铸相关统计
     */
    val reforge: ReforgeStats
}
package cc.mewcraft.wakame.item.binary.stats

import cc.mewcraft.wakame.annotation.InternalApi
import me.lucko.helper.shadows.nbt.CompoundShadowTag

interface ItemStatisticsHolder {
    @InternalApi
    val rootOrNull: CompoundShadowTag?

    @InternalApi
    val rootOrCreate: CompoundShadowTag

    /**
     * 实体击杀数
     */
    val entityKills: EntityKillsStatistics

    /**
     * 单次最高伤害
     */
    val peakDamage: PeakDamageStatistics

    /**
     * 重铸相关统计
     */
    val reforge: ReforgeStatistics
}
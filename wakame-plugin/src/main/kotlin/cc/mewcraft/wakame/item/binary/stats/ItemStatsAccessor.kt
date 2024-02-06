package cc.mewcraft.wakame.item.binary.stats

import me.lucko.helper.shadows.nbt.CompoundShadowTag

interface ItemStatsAccessor {
    /**
     * Encompassing all tags of this [ItemStatsAccessor].
     */
    val tags: CompoundShadowTag // 外部不应该读取该变量

    ////// All Statistics Categories //////

    /**
     * 实体击杀数
     */
    val entityKillsStats: EntityKillsStats

    /**
     * 单次最高伤害
     */
    val peakDamageStats: PeakDamageStats

    /**
     * 重铸相关统计
     */
    val reforgeStats: ReforgeStats
}
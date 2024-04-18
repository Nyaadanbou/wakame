package cc.mewcraft.wakame.item.binary.stats

import me.lucko.helper.shadows.nbt.CompoundShadowTag

@Suppress("PropertyName")
interface ItemStatisticsAccessor {
    /**
     * Gets the root tag of statistics or `null`, if it does not exist.
     */
    val rootOrNull: CompoundShadowTag?

    /**
     * Gets the root tag of statistics or create one, if it does not exist.
     */
    val rootOrCreate: CompoundShadowTag

    //
    // Statistics Accessors
    //

    /**
     * 实体击杀数
     */
    val ENTITY_KILLS: EntityKillsStatistics

    /**
     * 单次最高伤害
     */
    val PEAK_DAMAGE: PeakDamageStatistics

    /**
     * 重铸相关统计
     */
    val REFORGE: ReforgeStatistics
}
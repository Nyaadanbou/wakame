package cc.mewcraft.wakame.item.components.tracks

/**
 * 一个信息跟踪的类型.
 */
interface TrackType<T : Track> {
    /**
     * 用于 NBT 索引.
     */
    val id: String
}

/**
 * 信息跟踪的所有类型.
 */
object TrackTypes {
    /**
     * 实体击杀数.
     */
    val ENTITY_KILLS: TrackType<TrackEntityKills> = TrackEntityKills

    /**
     * 单次最高伤害.
     */
    val PEAK_DAMAGE: TrackType<TrackPeakDamage> = TrackPeakDamage

    /**
     * 重铸历史数据.
     */
    val REFORGE_HISTORY: TrackType<TrackReforgeHistory> = TrackReforgeHistory
}
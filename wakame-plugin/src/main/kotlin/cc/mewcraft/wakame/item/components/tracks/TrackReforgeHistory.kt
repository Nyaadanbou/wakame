package cc.mewcraft.wakame.item.components.tracks

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.wakame.item.StatisticsConstants
import cc.mewcraft.wakame.item.components.tracks.TrackReforgeHistory.Companion.TAG_SUCCESS_COUNT
import cc.mewcraft.wakame.item.components.tracks.TrackReforgeHistory.Companion.TAG_TOTAL_COST
import cc.mewcraft.wakame.util.CompoundTag

/**
 * 跟踪物品的重铸数据 (不同于词条栏里的重铸历史, 该重铸历史针对的是整个物品).
 */
interface TrackReforgeHistory : Track {

    /**
     * 物品重铸成功的总次数.
     */
    val successCount: Int
    fun setSuccessCount(count: Int): TrackReforgeHistory
    fun growSuccessCount(count: Int = 1): TrackReforgeHistory

    /**
     * 物品重铸的总花费.
     */
    val totalCost: Double
    fun setTotalCost(cost: Double): TrackReforgeHistory
    fun growTotalCost(cost: Double): TrackReforgeHistory

    companion object : TrackType<TrackReforgeHistory> {
        fun of(nbt: CompoundTag): TrackReforgeHistory {
            val successCount = nbt.getInt(TAG_SUCCESS_COUNT)
            val totalCost = nbt.getDouble(TAG_TOTAL_COST)
            return TrackReforgeHistoryImpl(successCount, totalCost)
        }

        fun of(successCount: Int = 0, totalCost: Double = 0.0): TrackReforgeHistory {
            return TrackReforgeHistoryImpl(successCount, totalCost)
        }

        const val TAG_SUCCESS_COUNT = "success_count"
        const val TAG_TOTAL_COST = "total_cost"

        override val id: String = StatisticsConstants.REFORGE_HISTORY
    }
}

private data class TrackReforgeHistoryImpl(
    override val successCount: Int,
    override val totalCost: Double,
) : TrackReforgeHistory {
    override fun setSuccessCount(count: Int): TrackReforgeHistory {
        return copy(successCount = count)
    }

    override fun growSuccessCount(count: Int): TrackReforgeHistory {
        return copy(successCount = this.successCount + count)
    }

    override fun setTotalCost(cost: Double): TrackReforgeHistory {
        return copy(totalCost = cost)
    }

    override fun growTotalCost(cost: Double): TrackReforgeHistory {
        return copy(totalCost = this.totalCost + cost)
    }

    override fun serializeAsTag(): Tag = CompoundTag {
        putInt(TAG_SUCCESS_COUNT, successCount)
        putDouble(TAG_TOTAL_COST, totalCost)
    }
}
package cc.mewcraft.wakame.item.components.tracks

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.item.StatisticsConstants
import cc.mewcraft.wakame.util.CompoundTag

/**
 * 跟踪物品的重铸数据 (不同于核孔里的重铸历史, 该重铸历史针对的是整个物品).
 */
data class TrackReforgeHistory(
    val successCount: Int,
    val totalCost: Double,
) : Track {

    companion object : TrackType<TrackReforgeHistory> {

        const val SUCCESS_COUNT_FIELD = "success_count"
        const val TOTAL_COST_FIELD = "total_cost"

        override val id: String = StatisticsConstants.REFORGE_HISTORY

        fun of(nbt: CompoundTag): TrackReforgeHistory {
            val successCount = nbt.getInt(SUCCESS_COUNT_FIELD)
            val totalCost = nbt.getDouble(TOTAL_COST_FIELD)
            return TrackReforgeHistory(successCount, totalCost)
        }

        fun of(successCount: Int = 0, totalCost: Double = 0.0): TrackReforgeHistory {
            return TrackReforgeHistory(successCount, totalCost)
        }
    }

    fun growSuccessCount(count: Int): TrackReforgeHistory {
        return copy(successCount = this.successCount + count)
    }

    fun growTotalCost(cost: Double): TrackReforgeHistory {
        return copy(totalCost = this.totalCost + cost)
    }

    override fun saveNbt(): CompoundTag {
        return CompoundTag {
            putInt(SUCCESS_COUNT_FIELD, successCount)
            putDouble(TOTAL_COST_FIELD, totalCost)
        }
    }

}

package cc.mewcraft.wakame.item.components.cells.reforge

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.wakame.item.BinarySerializable
import cc.mewcraft.wakame.item.ReforgeBinaryKeys
import cc.mewcraft.wakame.util.CompoundTag

// 开发日记 2024/7/5
// 经过讨论, 一个特定的词条栏的重铸配置应该是在配置文件里指定的 (通过词条栏的 id),
// 而不应该把重铸配置记录在物品的 NBT 里; NBT 只储存重铸的历史数据.
// 我们一致认为这样的架构是最好的 - 它既能随时修改词条栏的重铸选项,
// 也可以让词条栏的重铸变成一个支持持久状态的流程.

/**
 * 词条栏的重铸历史数据.
 */
interface ReforgeHistory : BinarySerializable {
    /**
     * 检查该重铸数据是否为空.
     */
    val isEmpty: Boolean

    /**
     * 重铸成功的次数.
     */
    val successCount: Int

    /**
     * 设置重铸成功的次数.
     */
    fun setSuccessCount(value: Int): ReforgeHistory

    /**
     * 重铸失败的次数.
     */
    val failureCount: Int

    /**
     * 设置重铸失败的次数.
     */
    fun setFailureCount(value: Int): ReforgeHistory

    companion object {
        /**
         * 返回一个空的 [ReforgeHistory].
         */
        fun empty(): ReforgeHistory {
            return Empty
        }

        /**
         * 从 NBT 创建一个 [ReforgeHistory].
         */
        fun of(nbt: CompoundTag): ReforgeHistory {
            if (nbt.isEmpty) {
                return Empty
            }
            val successCount = nbt.getInt(ReforgeBinaryKeys.SUCCESS_COUNT)
            val failureCount = nbt.getInt(ReforgeBinaryKeys.FAILURE_COUNT)
            return ReforgeHistoryImpl(successCount, failureCount)
        }
    }

    private data object Empty : ReforgeHistory {
        override val isEmpty: Boolean = true
        override val successCount: Int = 0
        override fun setSuccessCount(value: Int): ReforgeHistory = this
        override val failureCount: Int = 0
        override fun setFailureCount(value: Int): ReforgeHistory = this
        override fun serializeAsTag(): Tag = CompoundTag.create()
    }
}

private data class ReforgeHistoryImpl(
    override val successCount: Int,
    override val failureCount: Int,
) : ReforgeHistory {
    override val isEmpty: Boolean = false

    override fun setSuccessCount(value: Int): ReforgeHistory {
        return copy(successCount = value)
    }

    override fun setFailureCount(value: Int): ReforgeHistory {
        return copy(failureCount = value)
    }

    override fun serializeAsTag(): Tag = CompoundTag {
        putInt(ReforgeBinaryKeys.SUCCESS_COUNT, successCount)
        putInt(ReforgeBinaryKeys.FAILURE_COUNT, failureCount)
    }
}

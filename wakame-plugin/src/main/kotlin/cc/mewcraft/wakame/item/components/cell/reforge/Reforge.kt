package cc.mewcraft.wakame.item.components.cell.reforge

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.wakame.item.ReforgeBinaryKeys
import cc.mewcraft.wakame.item.TagLike
import cc.mewcraft.wakame.util.CompoundTag

/**
 * 包含重铸系统会用到的数据.
 */
interface Reforge : TagLike {
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
    fun setSuccessCount(value: Int): Reforge

    /**
     * 重铸失败的次数.
     */
    val failureCount: Int

    /**
     * 设置重铸失败的次数.
     */
    fun setFailureCount(value: Int): Reforge

    companion object {
        /**
         * 返回一个空的 [Reforge].
         */
        fun empty(): Reforge {
            return Empty
        }

        /**
         * 从 NBT 创建一个 [Reforge].
         */
        fun of(nbt: CompoundTag): Reforge {
            if (nbt.isEmpty) {
                return Empty
            }
            val successCount = nbt.getInt(ReforgeBinaryKeys.SUCCESS_COUNT)
            val failureCount = nbt.getInt(ReforgeBinaryKeys.FAILURE_COUNT)
            return Impl(successCount, failureCount)
        }
    }

    private data object Empty : Reforge {
        override val isEmpty: Boolean = true
        override val successCount: Int = 0
        override fun setSuccessCount(value: Int): Reforge = this
        override val failureCount: Int = 0
        override fun setFailureCount(value: Int): Reforge = this
        override fun asTag(): Tag = CompoundTag.create()
    }

    private data class Impl(
        override val successCount: Int,
        override val failureCount: Int,
    ) : Reforge {
        override val isEmpty: Boolean = false

        override fun setSuccessCount(value: Int): Reforge {
            return copy(successCount = value)
        }

        override fun setFailureCount(value: Int): Reforge {
            return copy(failureCount = value)
        }

        override fun asTag(): Tag = CompoundTag {
            putInt(ReforgeBinaryKeys.SUCCESS_COUNT, successCount)
            putInt(ReforgeBinaryKeys.FAILURE_COUNT, failureCount)
        }
    }
}

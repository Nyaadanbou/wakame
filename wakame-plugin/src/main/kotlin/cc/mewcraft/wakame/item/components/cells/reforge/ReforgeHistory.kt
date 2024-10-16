package cc.mewcraft.wakame.item.components.cells.reforge

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.BinarySerializable
import cc.mewcraft.wakame.item.ReforgeBinaryKeys
import cc.mewcraft.wakame.util.CompoundTag

// 开发日记 2024/7/5
// 经过讨论, 一个特定的词条栏的重铸配置应该是在配置文件里指定的 (通过词条栏的 id),
// 而不应该把重铸配置记录在物品的 NBT 里; NBT 只储存重铸的历史数据.
// 我们一致认为这样的架构是最好的 - 它既能随时修改词条栏的重铸选项,
// 也可以让词条栏的重铸变成一个支持持久状态的流程.

/**
 * 词条栏的重铸历史数据.
 *
 * 该对象不可变, 所有的修改操作都会返回一个新的 [ReforgeHistory] 实例.
 */
interface ReforgeHistory : BinarySerializable<CompoundTag> {
    /**
     * 检查该重铸数据是否为空.
     */
    val isEmpty: Boolean

    /**
     * 该词条栏有史以来被*定制*的次数.
     */
    val modCount: Int

    /**
     * 设置该词条栏有史以来被*定制*的次数.
     */
    fun setModCount(value: Int): ReforgeHistory

    /**
     * 增加该词条栏有史以来被*定制*的次数.
     */
    fun addModCount(value: Int): ReforgeHistory

    /**
     * 该词条栏有史以来被*重造*的次数.
     */
    val rerollCount: Int

    /**
     * 设置该词条栏有史以来被*重造*的次数.
     */
    fun setRerollCount(value: Int): ReforgeHistory

    /**
     * 增加该词条栏有史以来被*重造*的次数.
     */
    fun addRerollCount(value: Int): ReforgeHistory

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
            val modCount = nbt.getInt(ReforgeBinaryKeys.MOD_COUNT)
            val rerollCount = nbt.getInt(ReforgeBinaryKeys.REROLL_COUNT)
            return ReforgeHistoryImpl(modCount, rerollCount)
        }
    }

    private data object Empty : ReforgeHistory {
        override val isEmpty: Boolean = true
        override val modCount: Int = 0
        override fun setModCount(value: Int): ReforgeHistory = ReforgeHistoryImpl(value, rerollCount)
        override fun addModCount(value: Int): ReforgeHistory = setModCount(modCount + value)
        override val rerollCount: Int = 0
        override fun setRerollCount(value: Int): ReforgeHistory = ReforgeHistoryImpl(modCount, value)
        override fun addRerollCount(value: Int): ReforgeHistory = setRerollCount(rerollCount + value)
        override fun serializeAsTag(): CompoundTag = CompoundTag.create()
    }
}


/* Implementations */


private data class ReforgeHistoryImpl(
    override val modCount: Int,
    override val rerollCount: Int,
) : ReforgeHistory {
    override val isEmpty: Boolean = false

    override fun setModCount(value: Int): ReforgeHistory {
        return copy(modCount = value)
    }

    override fun addModCount(value: Int): ReforgeHistory {
        return setModCount(modCount + value)
    }

    override fun setRerollCount(value: Int): ReforgeHistory {
        return copy(rerollCount = value)
    }

    override fun addRerollCount(value: Int): ReforgeHistory {
        return setRerollCount(rerollCount + value)
    }

    override fun serializeAsTag(): CompoundTag = CompoundTag {
        putInt(ReforgeBinaryKeys.MOD_COUNT, modCount)
        putInt(ReforgeBinaryKeys.REROLL_COUNT, rerollCount)
    }
}

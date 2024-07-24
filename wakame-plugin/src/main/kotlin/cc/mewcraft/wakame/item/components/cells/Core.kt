package cc.mewcraft.wakame.item.components.cells

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.BinarySerializable
import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.display.TooltipProvider
import cc.mewcraft.wakame.item.CoreBinaryKeys
import cc.mewcraft.wakame.item.components.cells.cores.attribute.CoreAttribute
import cc.mewcraft.wakame.item.components.cells.cores.empty.CoreEmpty
import cc.mewcraft.wakame.item.components.cells.cores.noop.CoreNoop
import cc.mewcraft.wakame.item.components.cells.cores.skill.CoreSkill
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable

// 开发日记 2024/6/30
// Core 不再分为 nbt wrapper 和 data holder,
// 它就是一个非常纯净的不可变的数据类 (data class).
// 这样设计虽然创建对象的性能开销大一点, 但维护难度会
// 大幅度降低, 架构的调用难度也会小点.

/**
 * 代表一个词条栏中的核心. 核心是[词条栏][Cell]中提供具体效果的东西.
 */
interface Core : Keyed, Examinable, BinarySerializable, TooltipProvider.SingleWithName {
    /**
     * 核心的唯一标识.
     */
    override val key: Key

    /**
     * 该核心的类型.
     */
    val type: CoreType<*>

    /**
     * 检查该核心是否为无操作.
     *
     * 如果一个词条栏的核心是无操作, 意味着该词条栏的核心不应该被“使用” (目前仅用于序列化).
     */
    val isNoop: Boolean

    /**
     * 检查该核心是否为空核心.
     *
     * 如果一个词条栏的核心为空, 则该词条栏的核心可以被替换成其他的.
     */
    val isEmpty: Boolean

    companion object {
        /**
         * 返回一个空的核心.
         */
        fun empty(): Core {
            return CoreEmpty
        }

        /**
         * 构建一个 [Core].
         */
        fun of(nbt: CompoundTag): Core {
            if (nbt.isEmpty) {
                return CoreEmpty
            }
            val key = Key(nbt.getString(CoreBinaryKeys.CORE_IDENTIFIER))
            val ret = when {
                // 技术核心
                key == GenericKeys.NOOP -> CoreNoop
                key == GenericKeys.EMPTY -> CoreEmpty

                // 普通核心
                key.namespace() == Namespaces.ATTRIBUTE -> CoreAttribute(nbt)
                key.namespace() == Namespaces.SKILL -> CoreSkill(nbt)

                // 无法识别 NBT
                else -> throw IllegalArgumentException("Failed to parse NBT tag ${nbt.asString()}")
            }
            return ret
        }
    }
}

/**
 * 代表一个核心的类型.
 */
interface CoreType<T : Core>
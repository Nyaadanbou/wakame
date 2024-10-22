package cc.mewcraft.wakame.item.components.cells

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.*
import cc.mewcraft.wakame.attribute.composite.ConstantCompositeAttribute
import cc.mewcraft.wakame.item.components.cells.cores.*
import cc.mewcraft.wakame.skill.ConfiguredSkill
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable


/**
 * 检查该核心是否为 *virtual*. 设计上 *virtual* 核心不应该出现在游戏中, 仅用于控制物品生成.
 */
val Core.isVirtual: Boolean
    get() = this is VirtualCore

/**
 * 检查该核心是否为 *empty*. 设计上 *empty* 核心可以被(玩家使用重铸)替换成其他的核心.
 */
val Core.isEmpty: Boolean
    get() = this is EmptyCore

/**
 * 代表一个核孔中的核心. 核心是 [核孔][Cell] 中提供具体效果的东西.
 */
interface Core : Examinable, BinarySerializable<CompoundTag> {
    /**
     * 核心的唯一标识. 主要用于序列化实现.
     *
     * - 该对象的 [Key.namespace] 用来区分不同基本类型的核心
     *     - 例如: 属性, 技能...
     * - 该对象的 [Key.value] 用来区分同一类型下不同的实体
     *     - 例如对于属性: 攻击力属性, 防御力属性...
     *     - 例如对于技能: 火球术, 冰冻术...
     */
    val id: Key

    /**
     * 核心的显示名称, 实现上应该不包含具体数值.
     */
    val displayName: Component

    /**
     * 核心的完整描述, 实现上应该包含具体数值和机制说明.
     */
    val description: List<Component>

    /**
     * 检查该核心是否跟 [other] 相似. 具体结果由实现决定.
     */
    fun similarTo(other: Core): Boolean

    /**
     * 把该核心转换为 NBT, 拥有以下基本结构:
     *
     * ```NBT
     * string('id'): <key: 核心的唯一标识>
     * ...
     * ```
     */
    override fun serializeAsTag(): CompoundTag
}

/**
 * [VirtualCore] 代表永远不会被写入物品 NBT 的核心.
 *
 * 用来引导系统在生成萌芽物品时, 不要把当前核孔写入物品.
 * 因此这个核心永远不会出现在游戏内的物品上, 不然就是 BUG.
 */
interface VirtualCore : Core

/**
 * [EmptyCore] 是一个特殊核心, 表示这个核心不存在.
 *
 * 当一个核孔里没有核心时 (但核孔本身存在), 里面实际上存放了一颗空核心.
 * 玩家概念上的“核孔没有核心”, 就是技术概念上的 “核孔里装的是空核心”.
 */
interface EmptyCore : Core

/**
 * [AttributeCore] 是一个属性核心, 用于表示一个 [ConstantCompositeAttribute].
 */
interface AttributeCore : Core {
    /**
     * 该属性核心的属性种类及其数值.
     */
    val attribute: ConstantCompositeAttribute
}

/**
 * [SkillCore] 是一个技能核心, 用于表示一个 [ConfiguredSkill].
 */
interface SkillCore : Core {
    /**
     * 该技能核心的技能种类及其变体.
     */
    val skill: ConfiguredSkill
}

/**
 * 本单例用于构建 [Core] 的实例.
 */
object CoreFactory {
    /**
     * 返回一个 [VirtualCore].
     */
    fun virtual(): VirtualCore {
        return SimpleVirtualCore
    }

    /**
     * 返回一个 [EmptyCore].
     */
    fun empty(): EmptyCore {
        return SimpleEmptyCore
    }

    /**
     * 从 NBT 反序列化一个 [Core]. 给定的 NBT 结构必须如下:
     *
     * ```NBT
     * string('id'): <key: 核心的唯一标识>
     * ...
     * ```
     *
     * @param tag 包含核心数据的 NBT
     *
     * @return 反序列化出来的核心
     */
    fun deserialize(tag: CompoundTag): Core {
        if (tag.isEmpty)
            return SimpleEmptyCore

        val coreId = readCoreId(tag)
        val core = when {
            coreId == GenericKeys.EMPTY -> {
                SimpleEmptyCore
            }

            coreId.namespace() == Namespaces.ATTRIBUTE -> {
                AttributeCore(coreId, tag)
            }

            coreId.namespace() == Namespaces.SKILL -> {
                SkillCore(coreId, tag)
            }

            // 无法识别 NBT
            else -> {
                throw IllegalArgumentException("Failed to parse NBT tag ${tag.asString()}")
            }
        }

        return core
    }

    private fun readCoreId(nbt: CompoundTag): Key {
        return Key.key(nbt.getString(CoreConstants.NBT_CORE_ID))
    }
}

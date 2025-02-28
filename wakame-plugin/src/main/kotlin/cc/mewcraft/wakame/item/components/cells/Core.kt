package cc.mewcraft.wakame.item.components.cells

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.attribute.bundle.ConstantAttributeBundle
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.node
import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.util.Identifiers
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import org.spongepowered.configurate.ConfigurationNode


/**
 * 检查该核心是否为 [EmptyCore]. 设计上 [EmptyCore] 核心可以被(玩家使用重铸)替换成其他的核心.
 */
val Core.isEmpty: Boolean
    get() = this is EmptyCore

/**
 * 检查该核心是否为 [VirtualCore]. 设计上 [VirtualCore] 核心不应该出现在游戏中, 仅用于控制物品生成.
 */
val Core.isVirtual: Boolean
    get() = this is VirtualCore

/**
 * 核孔中的核心. 核心是 [核孔][Cell] 中提供实际效果的东西.
 */
interface Core {

    companion object {

        /**
         * 返回一个 [EmptyCore].
         */
        fun empty(): Core {
            return EmptyCore
        }

        /**
         * 返回一个 [VirtualCore].
         */
        fun virtual(): Core {
            return VirtualCore
        }

        /**
         * 从 NBT 创建一个 [Core]. 给定的 NBT 结构必须如下:
         *
         * ```NBT
         * string('id'): <key: 核心的唯一标识>
         * ...
         * ```
         *
         * @param nbt 包含核心数据的 NBT
         * @return 反序列化出来的核心
         */
        fun fromNbt(nbt: CompoundTag): Core {
            if (nbt.isEmpty) {
                return EmptyCore
            }

            val id = Identifiers.of(nbt.getString("id"))
            val core = when {
                id == GenericKeys.EMPTY -> EmptyCore
                id.namespace() == Namespaces.ATTRIBUTE -> AttributeCore.fromNbt(id, nbt)
                else -> throw IllegalArgumentException("Failed to parse NBT element $nbt")
            }

            return core
        }
    }

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
    fun saveNbt(): CompoundTag
}

/**
 * [VirtualCore] 代表永远不会被写入物品 NBT 的核心.
 *
 * 用来引导系统在生成萌芽物品时, 不要把当前核孔写入物品.
 * 因此这个核心永远不会出现在游戏内的物品上, 不然就是 BUG.
 */
data object VirtualCore : Core {
    private val config = ItemComponentConfig.provide(ItemConstants.CELLS).rootNode.node("virtual_core")

    override val id: Key = GenericKeys.NOOP
    override val displayName: Component by config.entry<Component>("display_name")
    override val description: List<Component> by config.entry<List<Component>>("description")

    override fun similarTo(other: Core): Boolean {
        return other is VirtualCore
    }

    override fun saveNbt(): CompoundTag {
        error("${this::class.simpleName} cannot be saved as NBT element")
    }
}

/**
 * [EmptyCore] 是一个特殊核心, 表示这个核心不存在.
 *
 * 当一个核孔里没有核心时 (但核孔本身存在), 里面实际上存放了一颗空核心.
 * 玩家概念上的“核孔没有核心”, 就是技术概念上的 “核孔里装的是空核心”.
 */
data object EmptyCore : Core {
    private val config = ItemComponentConfig.provide(ItemConstants.CELLS).rootNode.node("empty_core")

    override val id: Key = GenericKeys.EMPTY
    override val displayName: Component by config.entry<Component>("display_name")
    override val description: List<Component> by config.entry<List<Component>>("description")

    override fun similarTo(other: Core): Boolean {
        return other is EmptyCore
    }

    override fun saveNbt(): CompoundTag =
        CompoundTag()
}

/**
 * [AttributeCore] 是一个属性核心, 用于表示一个 [ConstantAttributeBundle].
 *
 * @property id 核心唯一标识
 * @property data 该属性核心的属性数据
 */
data class AttributeCore(
    override val id: Key,
    val data: ConstantAttributeBundle,
) : Core {
    override val displayName: Component
        get() = data.displayName
    override val description: List<Component>
        get() = data.description

    companion object {

        /**
         * 本函数用于从 NBT 构建 [AttributeCore].
         *
         * @param id 核心的唯一标识
         * @param nbt 包含核心数据的 NBT
         * @return 新的 [AttributeCore]
         * @see ConstantAttributeBundle 了解 NBT 结构
         */
        fun fromNbt(id: Key, nbt: CompoundTag): AttributeCore {
            val data = KoishRegistries.ATTRIBUTE_BUNDLE_FACADE.getOrThrow(id.value()).convertNbtToConstant(nbt)
            return AttributeCore(id, data)
        }

        /**
         * 本函数用于从配置文件构建 [AttributeCore].
         *
         * @param id 核心的唯一标识
         * @param node 包含核心数据的 Node
         * @return 新的 [AttributeCore]
         * @see ConstantAttributeBundle 了解 Node 结构
         */
        fun fromNode(id: Key, node: ConfigurationNode): AttributeCore {
            val data = KoishRegistries.ATTRIBUTE_BUNDLE_FACADE.getOrThrow(id.value()).convertNodeToConstant(node)
            return AttributeCore(id, data)
        }

    }

    /**
     * 检查两个属性核心是否拥有一样的:
     * - 运算模式
     * - 数值结构
     * - 元素类型 (如果存在)
     *
     * 该函数不会检查任何数值的相等性.
     */
    override fun similarTo(other: Core): Boolean {
        return other is AttributeCore && data.similarTo(other.data)
    }

    override fun saveNbt(): CompoundTag {
        val attributeTag = data.saveNbt()

        val baseTag = CompoundTag()
        baseTag.writeCoreId(id)
        baseTag.merge(attributeTag)

        return baseTag
    }

    private fun CompoundTag.writeCoreId(id: Key) {
        putString("id", id.toString())
    }
}

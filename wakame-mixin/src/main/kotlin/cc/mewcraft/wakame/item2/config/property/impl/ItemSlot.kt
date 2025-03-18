package cc.mewcraft.wakame.item2.config.property.impl

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.Namespaces
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import net.kyori.examination.string.StringExaminer
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import java.util.stream.Stream

/**
 * 代表一个可以让物品生效的 *玩家背包* 中的栏位.
 *
 * 如果一个物品在这个栏位里, 那么这个物品就应该被认为是“生效的”,
 * 所有的属性、技能、铭刻等都应该对当前物品的拥有者 (即玩家) 生效.
 *
 * 如果一个物品没有生效的栏位, 使用 [ItemSlot2.empty] 单例.
 */
/* sealed */ interface ItemSlot2 : Examinable {
    /**
     * 栏位的名字.
     */
    val id: Key

    /**
     * 获取该 [ItemSlot2] 所对应的玩家背包里的栏位.
     *
     * 参考:
     *
     * Converted Slots:
     * 39             1  2     0
     * 38             3  4
     * 37
     * 36          40
     * 9  10 11 12 13 14 15 16 17
     * 18 19 20 21 22 23 24 25 26
     * 27 28 29 30 31 32 33 34 35
     * 0  1  2  3  4  5  6  7  8
     */
    val slotIndex: Int

    /**
     * 检查给定的 [EquipmentSlot] 是否为有效的栏位.
     */
    fun testEquipmentSlot(slot: EquipmentSlot): Boolean =
        false // default returns false

    /**
     * 检查给定的 [EquipmentSlotGroup] 是否为有效的栏位.
     */
    fun testEquipmentSlotGroup(group: EquipmentSlotGroup): Boolean =
        false // default returns false

    /**
     * 检查给定的 [EquipmentSlotGroup] 集合是否为有效的栏位.
     */
    fun testEquipmentSlotGroup(groups: Set<EquipmentSlotGroup>): Boolean =
        groups.any { testEquipmentSlotGroup(it) }

    /**
     * 获取该 [ItemSlot2] 所对应的玩家背包里的物品.
     *
     * 该函数不返回 [ItemStack.isEmpty] 为 `true` 的物品.
     * 对于这种物品, 该函数一律返回 `null` 来表示它们.
     */
    fun getItem(player: Player): ItemStack?

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("id", id),
        ExaminableProperty.of("slot_index", slotIndex)
    )

    companion object {
        /**
         * 代表一个不存在的 [slotIndex].
         */
        const val EMPTY_SLOT_INDEX = -1

        /**
         * 获取一个不存在的 [ItemSlot2] 实例.
         *
         * 当一个物品对于玩家来说没有生效的栏位时, 应该使用这个.
         */
        fun empty(): ItemSlot2 {
            return Empty
        }

        /**
         * 获取一个虚拟的 [ItemSlot2] 实例.
         *
         * 该实例不用于配置文件序列化, 也不用于和其他系统的交互.
         * 目前仅用于生成属性修饰符的名字, 未来技能应该也会用到.
         */
        fun imaginary(): ItemSlot2 {
            return Imaginary
        }
    }

    /**
     * 代表一个不存在的 [ItemSlot2].
     *
     * 对玩家没有任何效果的物品 (例如材料) 应该使用这个 [ItemSlot2].
     */
    private data object Empty : ItemSlot2 {
        override val id: Key = GenericKeys.NOOP
        override val slotIndex: Int = EMPTY_SLOT_INDEX

        override fun getItem(player: Player): ItemStack? {
            return null
        }

        override fun toString(): String {
            return examine(StringExaminer.simpleEscaping())
        }
    }

    /**
     * 代表一个虚拟的 [ItemSlot2].
     */
    private data object Imaginary : ItemSlot2 {
        override val id: Key = Key.key(Namespaces.GENERIC, "imaginary")
        override val slotIndex: Int = 99

        override fun getItem(player: Player): ItemStack? {
            return null
        }

        override fun toString(): String {
            return examine(StringExaminer.simpleEscaping())
        }
    }
}

// 开发日记 2024/8/2
// 物品配置上, 储存的是 ItemSlotGroup, 而不是 ItemSlot2.
// 当判断一个物品是否在一个栏位上生效时, 先获取这个物品的 ItemSlotGroup,
// 然后再遍历这个 ItemSlotGroup 里的所有 ItemSlot2:
// 如果有一个 ItemSlot2 是生效的, 那整个就算作是生效的.

/**
 * 代表一组 [ItemSlot2], 直接储存在一个物品的模板中.
 */
interface ItemSlotGroup {

    /**
     * 包含用于创建 [ItemSlotGroup] 的函数.
     */
    companion object {
        /**
         * 获取一个空的 [ItemSlotGroup] 实例.
         */
        fun empty(): ItemSlotGroup {
            return EmptyItemSlotGroup
        }
    }

    val children: Set<ItemSlot2>

    /**
     * 检查给定的 [Key] 是否在这个组中.
     */
    fun contains(id: Key): Boolean

    /**
     * 检查给定的 [ItemSlot2] 是否在这个组中.
     */
    fun contains(itemSlot: ItemSlot2): Boolean

    /**
     * 检查给定的 [EquipmentSlot] 是否为有效的栏位.
     */
    fun test(slot: EquipmentSlot): Boolean

    /**
     * 检查给定的 [EquipmentSlotGroup] 是否为有效的栏位.
     */
    fun test(group: EquipmentSlotGroup): Boolean
}

private object EmptyItemSlotGroup : ItemSlotGroup {
    override val children: Set<ItemSlot2> = emptySet()
    override fun contains(id: Key): Boolean = false
    override fun contains(itemSlot: ItemSlot2): Boolean = false
    override fun test(slot: EquipmentSlot): Boolean = false
    override fun test(group: EquipmentSlotGroup): Boolean = false
}
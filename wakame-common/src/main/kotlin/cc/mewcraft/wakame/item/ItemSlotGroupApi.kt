@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.item

import net.kyori.adventure.key.Key
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup

// 开发日记 2024/8/2
// 物品配置上, 储存的是 ItemSlotGroup, 而不是 ItemSlot.
// 当判断一个物品是否在一个栏位上生效时, 先获取这个物品的 ItemSlotGroup,
// 然后再遍历这个 ItemSlotGroup 里的所有 ItemSlot:
// 如果有一个 ItemSlot 是生效的, 那整个就算作是生效的.

/**
 * 代表一组 [ItemSlot], 直接储存在一个物品的模板中.
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

    val children: Set<ItemSlot>

    /**
     * 检查给定的 [Key] 是否在这个组中.
     */
    fun contains(id: Key): Boolean

    /**
     * 检查给定的 [ItemSlot] 是否在这个组中.
     */
    fun contains(itemSlot: ItemSlot): Boolean

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
    override val children: Set<ItemSlot> = emptySet()
    override fun contains(id: Key): Boolean = false
    override fun contains(itemSlot: ItemSlot): Boolean = false
    override fun test(slot: EquipmentSlot): Boolean = false
    override fun test(group: EquipmentSlotGroup): Boolean = false
}
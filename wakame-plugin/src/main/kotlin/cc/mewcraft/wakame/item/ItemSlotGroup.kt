@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.util.krequire
import it.unimi.dsi.fastutil.objects.ReferenceArraySet
import net.kyori.adventure.key.Key
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.kotlin.extensions.getList
import java.lang.reflect.Type

// 开发日记 2024/8/2
// 物品配置上, 储存的是 ItemSlotGroup, 而不是 ItemSlot.
// 当判断一个物品是否在一个栏位上生效时, 先获取这个物品的 ItemSlotGroup,
// 然后再遍历这个 ItemSlotGroup 里的所有 ItemSlot:
// 如果有一个 ItemSlot 是生效的, 那整个就算作是生效的.

/**
 * 代表一组 [ItemSlot], 直接储存在 [NekoItem] 中.
 */
interface ItemSlotGroup {
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

private class SimpleItemSlotGroup(
    override val children: Set<ItemSlot>,
) : ItemSlotGroup {
    override fun contains(id: Key): Boolean {
        return children.any { it.id == id }
    }

    override fun contains(itemSlot: ItemSlot): Boolean {
        return children.contains(itemSlot)
    }

    override fun test(slot: EquipmentSlot): Boolean {
        return children.any { it.testEquipmentSlot(slot) }
    }

    override fun test(group: EquipmentSlotGroup): Boolean {
        return children.any { it.testEquipmentSlotGroup(group) }
    }
}

private object EmptyItemSlotGroup : ItemSlotGroup {
    override val children: Set<ItemSlot> = emptySet()
    override fun contains(id: Key): Boolean = false
    override fun contains(itemSlot: ItemSlot): Boolean = false
    override fun test(slot: EquipmentSlot): Boolean = false
    override fun test(group: EquipmentSlotGroup): Boolean = false
}

/**
 * [ItemSlotGroup] 的序列化器.
 */
internal object ItemSlotGroupSerializer : TypeSerializer<ItemSlotGroup> {
    override fun emptyValue(specificType: Type, options: ConfigurationOptions): ItemSlotGroup? {
        return EmptyItemSlotGroup
    }

    override fun deserialize(type: Type, node: ConfigurationNode): ItemSlotGroup {
        if (node.rawScalar() != null) {
            val single = node.krequire<ItemSlot>()
            return SimpleItemSlotGroup(setOf(single))
        }

        val multiple = node.getList<ItemSlot>(emptyList())

        return when (multiple.size) {
            0 -> SimpleItemSlotGroup(emptySet())
            1 -> SimpleItemSlotGroup(setOf(multiple[0]))
            else -> SimpleItemSlotGroup(ReferenceArraySet(multiple))
        }
    }
}
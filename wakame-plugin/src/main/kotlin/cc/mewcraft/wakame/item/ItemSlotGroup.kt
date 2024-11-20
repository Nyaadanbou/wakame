@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.util.krequire
import it.unimi.dsi.fastutil.objects.ReferenceArraySet
import it.unimi.dsi.fastutil.objects.ReferenceSets
import net.kyori.adventure.key.Key
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.kotlin.extensions.getList
import java.lang.reflect.Type

/**
 * [ItemSlotGroup] 的序列化器.
 */
object ItemSlotGroupSerializer : TypeSerializer<ItemSlotGroup> {
    override fun emptyValue(specificType: Type, options: ConfigurationOptions): ItemSlotGroup? {
        return ItemSlotGroup.empty()
    }

    override fun deserialize(type: Type, node: ConfigurationNode): ItemSlotGroup {
        if (node.rawScalar() != null) {
            val single = node.krequire<ItemSlot>()
            return SimpleItemSlotGroup(ReferenceSets.singleton(single))
        }

        val multiple = node.getList<ItemSlot>(emptyList())

        return when (multiple.size) {
            0 -> SimpleItemSlotGroup(ReferenceSets.emptySet())
            1 -> SimpleItemSlotGroup(ReferenceSets.singleton(multiple[0]))
            else -> SimpleItemSlotGroup(ReferenceArraySet(multiple))
        }
    }
}

/**
 * [ItemSlotGroup] 的一般实现.
 */
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
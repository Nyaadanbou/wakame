@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.initializer.Initializable
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger

/**
 * [ItemSlotRegistry] 的默认实现.
 */
object DefaultItemSlotRegistry : ItemSlotRegistry, Initializable, KoinComponent {
    private val logger: Logger by inject()

    // 所有的 ItemSlot 实例
    // 优化: 使用 ArraySet 来加快遍历的速度
    private val allSet: ObjectArraySet<ItemSlot> = ObjectArraySet()

    // 储存了 Minecraft 原版槽位的 ItemSlot
    private val vanillaBySlot: Reference2ReferenceOpenHashMap<EquipmentSlot, ItemSlot> = Reference2ReferenceOpenHashMap()
    private val vanillaByGroup: Reference2ReferenceOpenHashMap<EquipmentSlotGroup, ObjectArraySet<ItemSlot>> = Reference2ReferenceOpenHashMap()

    // 储存了除 Vanilla 之外的所有 ItemSlot
    private val custom: Int2ObjectOpenHashMap<ItemSlot> = Int2ObjectOpenHashMap()
    private val customSet = ObjectArraySet<ItemSlot>()

    override val size: Int
        get() = allSet.size

    override fun all(): Set<ItemSlot> {
        return allSet
    }

    override fun custom(): Set<ItemSlot> {
        return customSet
    }

    override fun get(group: EquipmentSlotGroup): Set<ItemSlot> {
        return vanillaByGroup[group] ?: emptySet()
    }

    override fun get(slot: EquipmentSlot): ItemSlot? {
        return vanillaBySlot[slot]
    }

    override fun get(slotIndex: Int): ItemSlot? {
        return custom[slotIndex]
    }

    override fun register(slot: ItemSlot) {
        if (allSet.add(slot)) {
            logger.info("Registered item slot: '${slot.id.asString()}'")
        }

        when (slot) {
            is VanillaItemSlot -> {
                vanillaBySlot.putIfAbsent(slot.slot, slot)
                vanillaByGroup.computeIfAbsent(slot.slot.group) {
                    ObjectArraySet()
                }.add(slot)
            }

            is CustomItemSlot -> {
                custom.putIfAbsent(slot.slotIndex, slot)
                customSet.add(slot)
            }

            else -> {
                throw IllegalArgumentException("Failed to register slot: '$slot'")
            }
        }
    }
}
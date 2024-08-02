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
 * 储存当前所有已加载的 [ItemSlot] 实例.
 *
 * [ItemSlot] 的实例在设计上是*按需创建*的. 每当序列化一个 [ItemSlot] 时,
 * 会自动注册到这个注册表中. 也就是说, 如果一个 [ItemSlot] 从未被序列化过,
 * 那么它就不会被注册到这个注册表中.
 */
object ItemSlotRegistry : Initializable, KoinComponent {
    private val logger: Logger by inject()

    // 所有的 ItemSlot 实例
    // 优化: 使用 ArraySet 来加快遍历的速度
    private val all: ObjectArraySet<ItemSlot> = ObjectArraySet()

    // 储存了 Minecraft 原版槽位的 ItemSlot
    private val vanillaBySlot: MutableMap<EquipmentSlot, ItemSlot> = Reference2ReferenceOpenHashMap()
    private val vanillaByGroup: MutableMap<EquipmentSlotGroup, ObjectArraySet<ItemSlot>> = Reference2ReferenceOpenHashMap()

    // 储存了除 Vanilla 之外的所有 ItemSlot
    private val custom: Int2ObjectOpenHashMap<ItemSlot> = Int2ObjectOpenHashMap()

    /**
     * 当前可用的 [ItemSlot] 的实例数.
     */
    val size: Int
        get() = all.size

    /**
     * 获取所有的 [ItemSlot].
     */
    fun all(): Set<ItemSlot> {
        return all
    }

    /**
     * 获取一个 `Equipment Slot Group` 对应的 [ItemSlot].
     */
    fun get(group: EquipmentSlotGroup): Set<ItemSlot> {
        return requireNotNull(vanillaByGroup[group]) { "Unknown slot for EquipmentSlotGroup: '$group'" }
    }

    /**
     * 获取一个 `Equipment Slot` 对应的 [ItemSlot].
     */
    fun get(slot: EquipmentSlot): ItemSlot {
        return requireNotNull(vanillaBySlot[slot]) { "Unknown slot for EquipmentSlot: '$slot'" }
    }

    /**
     * 获取一个跟 `Slot Number` 对应的 [ItemSlot].
     */
    fun get(slotIndex: Int): ItemSlot {
        return requireNotNull(custom[slotIndex]) { "Unknown slot for SlotIndex: '$slotIndex'" }
    }

    /**
     * 注册一个 [ItemSlot] 实例.
     *
     * 每当一个 [ItemSlot] 被创建时, 应该调用此函数.
     *
     * @param slot [ItemSlot] 实例
     */
    fun register(slot: ItemSlot) {
        if (all.add(slot)) {
            logger.info("Registered ItemSlot: $slot")
        } else {
            logger.warn("Failed to register ItemSlot: $slot")
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
            }

            else -> {
                throw IllegalArgumentException("Failed to register slot: '$slot'")
            }
        }
    }
}
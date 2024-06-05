package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.EquipmentSlot
import java.util.stream.Stream


/**
 * This is the effective slot that never takes effect.
 *
 * Used for items that provides no effects for players, such as materials.
 */
data object NoopItemSlot : ItemSlot

/**
 * The vanilla slots that are potentially effective for an item.
 */
enum class VanillaItemSlot : ItemSlot {
    MAIN_HAND {
        override fun testItemHeldEvent(player: Player, previousSlot: Int, newSlot: Int): Boolean {
            return player.inventory.heldItemSlot == previousSlot || player.inventory.heldItemSlot == newSlot
        }

        override fun testInventorySlotChangeEvent(player: Player, slot: Int, rawSlot: Int): Boolean {
            return player.inventory.heldItemSlot == slot && player.openInventory.getSlotType(rawSlot) == InventoryType.SlotType.QUICKBAR
        }

        override fun testEquipmentSlot(slot: EquipmentSlot): Boolean {
            return slot == EquipmentSlot.HAND
        }
    },
    OFF_HAND {
        override fun testInventorySlotChangeEvent(player: Player, slot: Int, rawSlot: Int): Boolean {
            return rawSlot == 45
        }

        override fun testEquipmentSlot(slot: EquipmentSlot): Boolean {
            return slot == EquipmentSlot.OFF_HAND
        }
    },
    HELMET {
        override fun testInventorySlotChangeEvent(player: Player, slot: Int, rawSlot: Int): Boolean {
            return rawSlot == 5
        }

        override fun testEquipmentSlot(slot: EquipmentSlot): Boolean {
            return slot == EquipmentSlot.HEAD
        }
    },
    CHESTPLATE {
        override fun testInventorySlotChangeEvent(player: Player, slot: Int, rawSlot: Int): Boolean {
            return rawSlot == 6
        }

        override fun testEquipmentSlot(slot: EquipmentSlot): Boolean {
            return slot == EquipmentSlot.CHEST
        }
    },
    LEGGINGS {
        override fun testInventorySlotChangeEvent(player: Player, slot: Int, rawSlot: Int): Boolean {
            return rawSlot == 7
        }

        override fun testEquipmentSlot(slot: EquipmentSlot): Boolean {
            return slot == EquipmentSlot.LEGS
        }
    },
    BOOTS {
        override fun testInventorySlotChangeEvent(player: Player, slot: Int, rawSlot: Int): Boolean {
            return rawSlot == 8
        }

        override fun testEquipmentSlot(slot: EquipmentSlot): Boolean {
            return slot == EquipmentSlot.FEET
        }
    },
    ;
}

/**
 * The accessory slots that are potentially effective for an item.
 */
data object AccessoryItemSlot {
    // Not currently in use, but it's a proof-of-concept
    // where we can easily introduce new ItemSlot.

    val SLOT_1: ItemSlot = object : ItemSlot {
        override fun testInventorySlotChangeEvent(player: Player, slot: Int, rawSlot: Int): Boolean {
            return rawSlot == 9
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return Stream.of(ExaminableProperty.of("name", "slot_1"))
        }

        override fun toString(): String {
            return toSimpleString()
        }
    }

    val SLOT_2: ItemSlot = object : ItemSlot {
        override fun testInventorySlotChangeEvent(player: Player, slot: Int, rawSlot: Int): Boolean {
            return rawSlot == 10
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return Stream.of(ExaminableProperty.of("name", "slot_2"))
        }

        override fun toString(): String {
            return toSimpleString()
        }
    }

    val SLOT_3: ItemSlot = object : ItemSlot {
        override fun testInventorySlotChangeEvent(player: Player, slot: Int, rawSlot: Int): Boolean {
            return rawSlot == 11
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return Stream.of(ExaminableProperty.of("name", "slot_3"))
        }

        override fun toString(): String {
            return toSimpleString()
        }
    }

    val SLOT_4: ItemSlot = object : ItemSlot {
        override fun testInventorySlotChangeEvent(player: Player, slot: Int, rawSlot: Int): Boolean {
            return rawSlot == 12
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return Stream.of(ExaminableProperty.of("name", "slot_4"))
        }

        override fun toString(): String {
            return toSimpleString()
        }
    }
}

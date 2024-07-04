package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.util.EnumLookup
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.EquipmentSlot
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * The vanilla slots that are potentially effective for an item.
 */
private enum class VanillaItemSlot : ItemSlot {
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

    override fun id(): String {
        return name
    }
}

/**
 * The accessory slots that are potentially effective for an item.
 */
internal data object AccessoryItemSlot {
    // Not currently in use, but it's a proof-of-concept
    // where we can easily introduce new ItemSlot.

    val SLOT_1: ItemSlot = object : ItemSlot {
        override fun id(): String {
            return "SLOT_1"
        }

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
        override fun id(): String {
            return "SLOT_2"
        }

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
        override fun id(): String {
            return "SLOT_3"
        }

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
        override fun id(): String {
            return "SLOT_4"
        }

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

/**
 * The serializer of [ItemSlot].
 */
internal object ItemSlotSerializer : SchemaSerializer<ItemSlot> {
    override fun emptyValue(specificType: Type, options: ConfigurationOptions): ItemSlot {
        return ItemSlot.noop()
    }

    override fun deserialize(type: Type, node: ConfigurationNode): ItemSlot {
        val rawString = node.krequire<String>()

        // check VanillaItemSlot
        val lookupResult = EnumLookup.lookup<VanillaItemSlot>(rawString)
        if (lookupResult.isSuccess) {
            return lookupResult.getOrThrow()
        }

        // check AccessoryItemSlot
        return when (rawString) {
            "SLOT_1" -> AccessoryItemSlot.SLOT_1
            "SLOT_2" -> AccessoryItemSlot.SLOT_2
            "SLOT_3" -> AccessoryItemSlot.SLOT_3
            "SLOT_4" -> AccessoryItemSlot.SLOT_4
            else -> throw SerializationException(node, type, "Invalid input for ItemSlot")
        }
    }
}
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
 * This is the effective slot that never takes effect.
 *
 * Used for items that provides no effects for players, such as materials.
 */
data object NoopEffectiveSlot : EffectiveSlot

/**
 * The vanilla slots that are potentially effective for an item.
 */
enum class VanillaEffectiveSlot : EffectiveSlot {
    MAIN_HAND {
        override fun testItemHeld(player: Player, previousSlot: Int, newSlot: Int): Boolean {
            return player.inventory.heldItemSlot == previousSlot || player.inventory.heldItemSlot == newSlot
        }

        override fun testInventorySlotChange(player: Player, slot: Int, rawSlot: Int): Boolean {
            return player.inventory.heldItemSlot == slot && player.openInventory.getSlotType(rawSlot) == InventoryType.SlotType.QUICKBAR
        }

        override fun testEquipmentSlotChange(slot: EquipmentSlot): Boolean {
            return slot == EquipmentSlot.HAND
        }
    },
    OFF_HAND {
        override fun testInventorySlotChange(player: Player, slot: Int, rawSlot: Int): Boolean {
            return rawSlot == 45
        }

        override fun testEquipmentSlotChange(slot: EquipmentSlot): Boolean {
            return slot == EquipmentSlot.OFF_HAND
        }
    },
    HELMET {
        override fun testInventorySlotChange(player: Player, slot: Int, rawSlot: Int): Boolean {
            return rawSlot == 5
        }

        override fun testEquipmentSlotChange(slot: EquipmentSlot): Boolean {
            return slot == EquipmentSlot.HEAD
        }
    },
    CHESTPLATE {
        override fun testInventorySlotChange(player: Player, slot: Int, rawSlot: Int): Boolean {
            return rawSlot == 6
        }

        override fun testEquipmentSlotChange(slot: EquipmentSlot): Boolean {
            return slot == EquipmentSlot.CHEST
        }
    },
    LEGGINGS {
        override fun testInventorySlotChange(player: Player, slot: Int, rawSlot: Int): Boolean {
            return rawSlot == 7
        }

        override fun testEquipmentSlotChange(slot: EquipmentSlot): Boolean {
            return slot == EquipmentSlot.LEGS
        }
    },
    BOOTS {
        override fun testInventorySlotChange(player: Player, slot: Int, rawSlot: Int): Boolean {
            return rawSlot == 8
        }

        override fun testEquipmentSlotChange(slot: EquipmentSlot): Boolean {
            return slot == EquipmentSlot.FEET
        }
    },
    ;
}

/**
 * The accessory slots that are potentially effective for an item.
 */
data object AccessoryEffectiveSlot {
    // Not currently in use, but it's a proof-of-concept
    // where we can easily introduce new EffectiveSlot.

    val SLOT_1: EffectiveSlot = object : EffectiveSlot {
        override fun testInventorySlotChange(player: Player, slot: Int, rawSlot: Int): Boolean {
            return rawSlot == 9
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return Stream.of(ExaminableProperty.of("name", "slot_1"))
        }

        override fun toString(): String {
            return toSimpleString()
        }
    }
    val SLOT_2: EffectiveSlot = object : EffectiveSlot {
        override fun testInventorySlotChange(player: Player, slot: Int, rawSlot: Int): Boolean {
            return rawSlot == 10
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return Stream.of(ExaminableProperty.of("name", "slot_2"))
        }

        override fun toString(): String {
            return toSimpleString()
        }
    }
    val SLOT_3: EffectiveSlot = object : EffectiveSlot {
        override fun testInventorySlotChange(player: Player, slot: Int, rawSlot: Int): Boolean {
            return rawSlot == 11
        }

        override fun examinableProperties(): Stream<out ExaminableProperty> {
            return Stream.of(ExaminableProperty.of("name", "slot_3"))
        }

        override fun toString(): String {
            return toSimpleString()
        }
    }
    val SLOT_4: EffectiveSlot = object : EffectiveSlot {
        override fun testInventorySlotChange(player: Player, slot: Int, rawSlot: Int): Boolean {
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
 * The serializer of [EffectiveSlot].
 */
data object EffectiveSlotSerializer : SchemaSerializer<EffectiveSlot> {
    override fun emptyValue(specificType: Type, options: ConfigurationOptions): EffectiveSlot {
        return NoopEffectiveSlot
    }

    override fun deserialize(type: Type, node: ConfigurationNode): EffectiveSlot {
        val rawString = node.krequire<String>()

        // check VanillaEffectiveSlot
        val lookupResult = EnumLookup.lookup<VanillaEffectiveSlot>(rawString)
        if (lookupResult.isSuccess) {
            return lookupResult.getOrThrow()
        }

        // check AccessoryEffectiveSlot
        return when (rawString) {
            "SLOT_1" -> AccessoryEffectiveSlot.SLOT_1
            "SLOT_2" -> AccessoryEffectiveSlot.SLOT_2
            "SLOT_3" -> AccessoryEffectiveSlot.SLOT_3
            "SLOT_4" -> AccessoryEffectiveSlot.SLOT_4
            else -> throw SerializationException(node, type, "Invalid input for EffectiveSlot")
        }
    }
}

package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.SchemeSerializer
import cc.mewcraft.wakame.util.EnumLookup
import cc.mewcraft.wakame.util.requireKt
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * Represents an inventory slot which is potentially effective for an item.
 *
 * If an item is **in** the effective slot, the item should be considered "effective"
 * so that all the attributes, abilities, kizamis, etc. should become effective
 * for the current item holder, i.e., the player.
 *
 * If an item has no effective slot, use the [NoopEffectiveSlot] singleton.
 */
interface EffectiveSlot {
    /*
     * Raw Slots:
     *
     * 5             1  2     0
     * 6             3  4
     * 7
     * 8           45
     * 9  10 11 12 13 14 15 16 17
     * 18 19 20 21 22 23 24 25 26
     * 27 28 29 30 31 32 33 34 35
     * 36 37 38 39 40 41 42 43 44
     */

    /*
     * Converted Slots:
     *
     * 39             1  2     0
     * 38             3  4
     * 37
     * 36          40
     * 9  10 11 12 13 14 15 16 17
     * 18 19 20 21 22 23 24 25 26
     * 27 28 29 30 31 32 33 34 35
     * 0  1  2  3  4  5  6  7  8
     */

    /**
     * Checks if the given parameters is referring to the effective slot.
     *
     * This function should be called when the held item slot changes.
     */
    fun testItemHeld(player: Player, previousSlot: Int, newSlot: Int): Boolean = false // default returns false

    /**
     * Checks if the given parameters is referring to the effective slot.
     *
     * This function should be called when the slot contents changes.
     */
    fun testInventorySlotChange(player: Player, slot: Int, rawSlot: Int): Boolean = false // default returns false
}

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
    },
    OFF_HAND {
        override fun testInventorySlotChange(player: Player, slot: Int, rawSlot: Int): Boolean {
            return rawSlot == 45
        }
    },
    HELMET {
        override fun testInventorySlotChange(player: Player, slot: Int, rawSlot: Int): Boolean {
            return rawSlot == 5
        }
    },
    CHESTPLATE {
        override fun testInventorySlotChange(player: Player, slot: Int, rawSlot: Int): Boolean {
            return rawSlot == 6
        }
    },
    LEGGINGS {
        override fun testInventorySlotChange(player: Player, slot: Int, rawSlot: Int): Boolean {
            return rawSlot == 7
        }
    },
    BOOTS {
        override fun testInventorySlotChange(player: Player, slot: Int, rawSlot: Int): Boolean {
            return rawSlot == 8
        }
    },
    ;
}

/**
 * This is the effective slot that never takes effect.
 *
 * Used for items that provides no effects for players, such as materials.
 */
data object NoopEffectiveSlot : EffectiveSlot

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
    }
    val SLOT_2: EffectiveSlot = object : EffectiveSlot {
        override fun testInventorySlotChange(player: Player, slot: Int, rawSlot: Int): Boolean {
            return rawSlot == 10
        }
    }
    val SLOT_3: EffectiveSlot = object : EffectiveSlot {
        override fun testInventorySlotChange(player: Player, slot: Int, rawSlot: Int): Boolean {
            return rawSlot == 11
        }
    }
    val SLOT_4: EffectiveSlot = object : EffectiveSlot {
        override fun testInventorySlotChange(player: Player, slot: Int, rawSlot: Int): Boolean {
            return rawSlot == 12
        }
    }
}

data object EffectiveSlotSerializer : SchemeSerializer<EffectiveSlot> {
    override fun emptyValue(specificType: Type, options: ConfigurationOptions): EffectiveSlot {
        return NoopEffectiveSlot
    }

    override fun deserialize(type: Type, node: ConfigurationNode): EffectiveSlot {
        val rawString = node.requireKt<String>()

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

package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.util.EnumLookup
import cc.mewcraft.wakame.util.krequire
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type


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

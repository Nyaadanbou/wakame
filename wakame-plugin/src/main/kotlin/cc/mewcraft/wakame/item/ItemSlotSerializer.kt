package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.SchemaSerializer
import cc.mewcraft.wakame.util.EnumLookup
import cc.mewcraft.wakame.util.krequire
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type

/**
 * The serializer of [ItemSlot].
 */
object ItemSlotSerializer : SchemaSerializer<ItemSlot> {
    override fun emptyValue(specificType: Type, options: ConfigurationOptions): ItemSlot {
        return NoopItemSlot
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
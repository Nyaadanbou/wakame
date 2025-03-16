package cc.mewcraft.wakame.serialization.json.serializer

import com.google.gson.*
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Type
import java.util.*

internal object ItemStackSerialization : JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    override fun serialize(src: ItemStack, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(Base64.getEncoder().encodeToString(src.serializeAsBytes()))
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ItemStack {
        return ItemStack.deserializeBytes(Base64.getDecoder().decode(json.asString))
    }

}
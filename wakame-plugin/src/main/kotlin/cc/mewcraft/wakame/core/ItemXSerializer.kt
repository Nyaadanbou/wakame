package cc.mewcraft.wakame.core

import cc.mewcraft.wakame.util.typeTokenOf
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.function.Predicate

object ItemXSerializer : ScalarSerializer<ItemX>(typeTokenOf<ItemX>()) {
    override fun serialize(item: ItemX?, typeSupported: Predicate<Class<*>>): Any {
        return item?.uid() ?: throw SerializationException("Can't serialize null object for ItemX")
    }

    override fun deserialize(type: Type, obj: Any?): ItemX {
        val uid = obj?.toString() ?: throw SerializationException(type, "Can't deserialize null value for ItemX")
        val itemX = ItemXRegistry.byUid(uid) ?: throw SerializationException(type, "Cannot deserialize string '$uid' into ItemX")
        return itemX
    }
}
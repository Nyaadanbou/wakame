package cc.mewcraft.wakame.serialization.configurate.typeserializer

import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.function.Predicate

internal object KeySerializer : ScalarSerializer<Key>(Key::class.java) {
    override fun deserialize(type: Type, obj: Any): Key {
        return try {
            Key.key(obj.toString())
        } catch (e: InvalidKeyException) {
            throw SerializationException(type, "Invalid key: '$obj'", e)
        }
    }

    override fun serialize(item: Key, typeSupported: Predicate<Class<*>>?): Any {
        return item.toString()
    }
}

internal object NamespacedKeySerializer : ScalarSerializer<NamespacedKey>(NamespacedKey::class.java) {
    override fun deserialize(type: Type, obj: Any): NamespacedKey {
        return NamespacedKey.fromString(obj.toString()) ?: throw IllegalArgumentException("Invalid key: '$obj'")
    }

    override fun serialize(item: NamespacedKey, typeSupported: Predicate<Class<*>>): Any {
        return item.toString()
    }
}
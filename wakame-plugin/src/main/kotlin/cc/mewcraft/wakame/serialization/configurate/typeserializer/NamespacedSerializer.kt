package cc.mewcraft.wakame.serialization.configurate.typeserializer

import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.KOISH_NAMESPACE
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
            Identifiers.of(obj.toString()) // 默认使用 koish 命名空间
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
        val string = obj.toString()
        val index = string.indexOf(':');
        val namespace = if (index >= 1) string.substring(0, index) else KOISH_NAMESPACE // 默认使用 koish 命名空间
        val value = if (index >= 0) string.substring(index + 1) else string
        return NamespacedKey(namespace, value)
    }

    override fun serialize(item: NamespacedKey, typeSupported: Predicate<Class<*>>): Any {
        return item.toString()
    }
}
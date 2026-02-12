package cc.mewcraft.wakame.serialization.configurate.serializer

import cc.mewcraft.wakame.util.KOISH_NAMESPACE
import cc.mewcraft.wakame.util.KoishKey
import cc.mewcraft.wakame.util.KoishKeys
import net.kyori.adventure.key.InvalidKeyException
import net.minecraft.resources.ResourceLocation
import org.bukkit.NamespacedKey
import org.spongepowered.configurate.serialize.ScalarSerializer
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type
import java.util.function.Predicate

/*internal*/ object IdentifierSerializer : ScalarSerializer<KoishKey>(KoishKey::class.java) {
    override fun deserialize(type: Type, obj: Any): KoishKey {
        return try {
            KoishKeys.of(obj.toString()) // 默认使用 "koish" 命名空间
        } catch (e: InvalidKeyException) {
            throw SerializationException(type, "Invalid key: '$obj'", e)
        }
    }

    override fun serialize(item: KoishKey, typeSupported: Predicate<Class<*>>?): Any {
        return item.toString()
    }
}

/*internal*/ object NamespacedKeySerializer : ScalarSerializer<NamespacedKey>(NamespacedKey::class.java) {
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

object ResourceLocationSerializer : ScalarSerializer<ResourceLocation>(ResourceLocation::class.java) {
    override fun deserialize(type: Type, obj: Any): ResourceLocation {
        val string = obj.toString()
        return ResourceLocation.parse(string)
    }

    override fun serialize(item: ResourceLocation, typeSupported: Predicate<Class<*>>): Any {
        return item.toString()
    }
}
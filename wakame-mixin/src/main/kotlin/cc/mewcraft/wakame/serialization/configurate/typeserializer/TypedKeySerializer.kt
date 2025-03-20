package cc.mewcraft.wakame.serialization.configurate.typeserializer

import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.javaTypeOf
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.serialize.SerializationException

// 这些不是严格意义上的 TypeSerializer, 只是简化了从 ConfigurationNode 获取一个 TypedKey<T> 的写法

/*internal*/ fun <E : Any> ConfigurationNode.requireTypedKey(registryKey: RegistryKey<E>): TypedKey<E> {
    val typedKey = getTypedKey(registryKey)
    if (typedKey == null) throw SerializationException(this, javaTypeOf<TypedKey<E>>(), "Cannot deserialize the node into a TypedKey<E>")
    return typedKey
}

/*internal*/ fun <E : Any> ConfigurationNode.getTypedKey(registryKey: RegistryKey<E>): TypedKey<E>? {
    val id = get<Identifier>()
    if (id == null) return null
    return TypedKey.create(registryKey, id)
}

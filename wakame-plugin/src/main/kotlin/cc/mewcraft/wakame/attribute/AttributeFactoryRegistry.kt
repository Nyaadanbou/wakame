package cc.mewcraft.wakame.attribute

import net.kyori.adventure.key.Key
import java.util.concurrent.ConcurrentHashMap

// TODO 合并到 AttributeCoreCodecs?
/**
 * Provides access to the implementations of [AttributeFactory].
 */
object AttributeFactoryRegistry {
    private val providers: ConcurrentHashMap<Key, AttributeFactory<out BinaryAttributeValue>> = ConcurrentHashMap()

    @Suppress("UNCHECKED_CAST")
    fun <T : BinaryAttributeValue> get(key: Key): AttributeFactory<T>? {
        return providers[key] as? AttributeFactory<T>
    }

    fun <T : BinaryAttributeValue> getOrThrow(key: Key): AttributeFactory<T> {
        return requireNotNull(get(key)) { "Can't find attribute modifier provider for $key" }
    }

    fun register(key: Key, provider: AttributeFactory<*>) {
        providers[key] = provider
    }

    fun unregister(key: Key) {
        providers.remove(key)
    }
}
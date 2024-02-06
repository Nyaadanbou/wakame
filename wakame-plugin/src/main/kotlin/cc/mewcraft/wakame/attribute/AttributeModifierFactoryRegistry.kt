package cc.mewcraft.wakame.attribute

import net.kyori.adventure.key.Key
import java.util.concurrent.ConcurrentHashMap

// TODO 合并到 AttributeCoreCodecs?
/**
 * Provides access to the implementations of [AttributeModifierFactory].
 */
object AttributeModifierFactoryRegistry {
    private val providers: ConcurrentHashMap<Key, AttributeModifierFactory<out BinaryAttributeValue>> = ConcurrentHashMap()

    @Suppress("UNCHECKED_CAST")
    fun <T : BinaryAttributeValue> get(key: Key): AttributeModifierFactory<T>? {
        return providers[key] as? AttributeModifierFactory<T>
    }

    fun <T : BinaryAttributeValue> getOrThrow(key: Key): AttributeModifierFactory<T> {
        return requireNotNull(get(key)) { "Can't find attribute modifier provider for $key" }
    }

    fun register(key: Key, provider: AttributeModifierFactory<*>) {
        providers[key] = provider
    }

    fun unregister(key: Key) {
        providers.remove(key)
    }
}
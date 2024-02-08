package cc.mewcraft.wakame.attribute

import net.kyori.adventure.key.Key
import java.util.concurrent.ConcurrentHashMap

/**
 * Registry of [AttributeFacade] implementations.
 */
object AttributeCoreCodecRegistry {
    @PublishedApi
    internal val codecs: ConcurrentHashMap<Key, AttributeFacade<out BinaryAttributeValue, out SchemeAttributeValue>> = ConcurrentHashMap()

    /**
     * Gets the specified [AttributeFacade].
     *
     * @param key the key of the [AttributeFacade]
     * @return the specified [AttributeFacade]
     */
    inline fun <reified C : AttributeFacade<out BinaryAttributeValue, out SchemeAttributeValue>> get(key: Key): C? {
        return codecs[key] as? C
    }

    /**
     * Gets the specified [AttributeFacade].
     *
     * @param key the key of the [AttributeFacade]
     * @return the specified [AttributeFacade]
     * @throws IllegalArgumentException if the key has no corresponding
     *     implementation.
     */
    inline fun <reified C : AttributeFacade<out BinaryAttributeValue, out SchemeAttributeValue>> getOrThrow(key: Key): C {
        return requireNotNull(get(key)) { "Can't find codec for key $key" }
    }

    /**
     * Register a [AttributeFacade], overwriting any existing one.
     *
     * @param key the key to the [AttributeFacade]
     * @param codec the [AttributeFacade] to register
     */
    fun register(key: Key, codec: AttributeFacade<out BinaryAttributeValue, out SchemeAttributeValue>) {
        codecs[key] = codec
    }
}
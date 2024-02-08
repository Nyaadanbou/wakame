package cc.mewcraft.wakame.attribute

import net.kyori.adventure.key.Key
import java.util.concurrent.ConcurrentHashMap

/**
 * Registry of [AttributeCoreCodec] implementations.
 */
object AttributeCoreCodecRegistry {
    @PublishedApi
    internal val codecs: ConcurrentHashMap<Key, AttributeCoreCodec<out BinaryAttributeValue, out SchemeAttributeValue>> = ConcurrentHashMap()

    /**
     * Gets the specified [AttributeCoreCodec].
     *
     * @param key the key of the [AttributeCoreCodec]
     * @return the specified [AttributeCoreCodec]
     */
    inline fun <reified C : AttributeCoreCodec<out BinaryAttributeValue, out SchemeAttributeValue>> get(key: Key): C? {
        return codecs[key] as? C
    }

    /**
     * Gets the specified [AttributeCoreCodec].
     *
     * @param key the key of the [AttributeCoreCodec]
     * @return the specified [AttributeCoreCodec]
     * @throws IllegalArgumentException if the key has no corresponding
     *     implementation.
     */
    inline fun <reified C : AttributeCoreCodec<out BinaryAttributeValue, out SchemeAttributeValue>> getOrThrow(key: Key): C {
        return requireNotNull(get(key)) { "Can't find codec for key $key" }
    }

    /**
     * Register a [AttributeCoreCodec], overwriting any existing one.
     *
     * @param key the key to the [AttributeCoreCodec]
     * @param codec the [AttributeCoreCodec] to register
     */
    fun register(key: Key, codec: AttributeCoreCodec<out BinaryAttributeValue, out SchemeAttributeValue>) {
        codecs[key] = codec
    }
}
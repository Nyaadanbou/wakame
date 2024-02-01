package cc.mewcraft.wakame.attribute.instance

import cc.mewcraft.wakame.attribute.AttributeBinaryValue
import cc.mewcraft.wakame.attribute.AttributeSchemaValue
import net.kyori.adventure.key.Key
import java.util.concurrent.ConcurrentHashMap

/**
 * Registry of [AttributeTangCodec] implementations.
 */
object AttributeTangCodecRegistry {
    @PublishedApi
    internal val codecs: ConcurrentHashMap<Key, AttributeTangCodec<out AttributeBinaryValue, out AttributeSchemaValue>> = ConcurrentHashMap()

    /**
     * Gets the specified [AttributeTangCodec].
     *
     * @param key the key of the [AttributeTangCodec]
     * @return the specified [AttributeTangCodec]
     */
    inline fun <reified C : AttributeTangCodec<out AttributeBinaryValue, out AttributeSchemaValue>> get(key: Key): C? {
        return codecs[key] as? C
    }

    /**
     * Gets the specified [AttributeTangCodec].
     *
     * @param key the key of the [AttributeTangCodec]
     * @return the specified [AttributeTangCodec]
     * @throws IllegalArgumentException if the key has no corresponding
     *     implementation.
     */
    inline fun <reified C : AttributeTangCodec<out AttributeBinaryValue, out AttributeSchemaValue>> getOrThrow(key: Key): C {
        return requireNotNull(get(key)) { "Can't find codec for key $key" }
    }

    /**
     * Register a [AttributeTangCodec], overwriting any existing one.
     *
     * @param key the key to the [AttributeTangCodec]
     * @param codec the [AttributeTangCodec] to register
     */
    fun register(key: Key, codec: AttributeTangCodec<out AttributeBinaryValue, out AttributeSchemaValue>) {
        codecs[key] = codec
    }
}
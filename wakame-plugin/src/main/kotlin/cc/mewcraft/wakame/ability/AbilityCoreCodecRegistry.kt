package cc.mewcraft.wakame.ability

import net.kyori.adventure.key.Key
import java.util.concurrent.ConcurrentHashMap

/**
 * Registry of [AbilityCoreCodec] implementations.
 */
object AbilityCoreCodecRegistry {
    @PublishedApi
    internal val codecs: ConcurrentHashMap<Key, AbilityCoreCodec<out AbilityBinaryValue, out AbilitySchemeValue>> = ConcurrentHashMap()

    /**
     * Gets the specified [AbilityCoreCodec].
     *
     * @param key the key of the [AbilityCoreCodec]
     * @return the specified [AbilityCoreCodec]
     */
    inline fun <reified T : AbilityCoreCodec<out AbilityBinaryValue, out AbilitySchemeValue>> get(key: Key): T? {
        return codecs[key] as T?
    }

    /**
     * Gets the specified [AbilityCoreCodec].
     *
     * @param key the key of the [AbilityCoreCodec]
     * @return the specified [AbilityCoreCodec]
     * @throws IllegalArgumentException if the key has no corresponding
     *     implementation.
     */
    inline fun <reified T : AbilityCoreCodec<out AbilityBinaryValue, out AbilitySchemeValue>> getOrThrow(key: Key): T {
        return requireNotNull(get(key)) { "Can't find implementation for $key" }
    }

    /**
     * Register a [AbilityCoreCodec], overwriting any existing one.
     *
     * @param key the key to the [AbilityCoreCodec]
     * @param codec the [AbilityCoreCodec] to register
     */
    fun register(key: Key, codec: AbilityCoreCodec<out AbilityBinaryValue, out AbilitySchemeValue>) {
        codecs[key] = codec
    }
}
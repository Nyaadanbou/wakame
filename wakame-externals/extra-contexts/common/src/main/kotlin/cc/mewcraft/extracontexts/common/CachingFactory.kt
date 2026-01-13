package cc.mewcraft.extracontexts.common

import cc.mewcraft.extracontexts.api.KeyValueStoreContextProvider
import cc.mewcraft.extracontexts.api.KeyValueStoreManager
import cc.mewcraft.extracontexts.common.context.CachedKeyValueStoreContextProvider
import cc.mewcraft.extracontexts.common.context.SimpleKeyValueStoreContextProvider
import cc.mewcraft.extracontexts.common.storage.CachedKeyValueStoreManager
import cc.mewcraft.extracontexts.common.storage.SimpleKeyValueStoreManager

/**
 * Factory for creating cached implementations of storage interfaces.
 *
 * This provides a convenient way to wrap storage implementations with caching layers.
 */
object CachingFactory {

    /**
     * Create a cached wrapper for KeyValueStoreManager.
     *
     * @param delegate The underlying implementation to wrap
     * @param expireAfterWriteMinutes Cache expiration time in minutes (default: 5)
     * @param maximumSize Maximum number of cache entries (default: 10000)
     * @return A cached version of the manager
     */
    fun createCachedKeyValueStoreManager(
        delegate: KeyValueStoreManager = SimpleKeyValueStoreManager,
        expireAfterWriteMinutes: Long = 5,
        maximumSize: Long = 10000,
    ): KeyValueStoreManager {
        return CachedKeyValueStoreManager(delegate, expireAfterWriteMinutes, maximumSize)
    }

    /**
     * Create a cached wrapper for KeyValueStoreContextProvider.
     *
     * @param delegate The underlying implementation to wrap
     * @param expireAfterWriteMinutes Cache expiration time in minutes (default: 5)
     * @param maximumSize Maximum number of cache entries (default: 10000)
     * @return A cached version of the provider
     */
    fun createCachedContextProvider(
        delegate: KeyValueStoreContextProvider = SimpleKeyValueStoreContextProvider(),
        expireAfterWriteMinutes: Long = 5,
        maximumSize: Long = 10000,
    ): KeyValueStoreContextProvider {
        return CachedKeyValueStoreContextProvider(delegate, expireAfterWriteMinutes, maximumSize)
    }

    /**
     * Create both cached managers for a complete caching setup.
     *
     * @param expireAfterWriteMinutes Cache expiration time in minutes (default: 5)
     * @param maximumSize Maximum number of cache entries (default: 10000)
     * @return A pair of (cachedKeyValueStoreManager, cachedContextProvider)
     */
    fun createCachedPair(
        expireAfterWriteMinutes: Long = 5,
        maximumSize: Long = 10000,
    ): Pair<KeyValueStoreManager, KeyValueStoreContextProvider> {
        val manager = createCachedKeyValueStoreManager(SimpleKeyValueStoreManager, expireAfterWriteMinutes, maximumSize)
        val provider = createCachedContextProvider(SimpleKeyValueStoreContextProvider(), expireAfterWriteMinutes, maximumSize)
        return manager to provider
    }
}


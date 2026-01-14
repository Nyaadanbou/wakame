package cc.mewcraft.extracontexts.common

import cc.mewcraft.extracontexts.api.KeyValueStoreManager
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
}


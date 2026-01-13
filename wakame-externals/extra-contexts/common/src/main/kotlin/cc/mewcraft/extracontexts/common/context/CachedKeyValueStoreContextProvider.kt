package cc.mewcraft.extracontexts.common.context

import cc.mewcraft.extracontexts.api.KeyValueStoreContextProvider
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Cached wrapper for KeyValueStoreContextProvider using Caffeine cache.
 *
 * Provides an in-memory cache layer to improve performance by reducing database queries.
 *
 * @param delegate The underlying KeyValueStoreContextProvider implementation
 * @param expireAfterWriteMinutes Cache expiration time in minutes (default: 5 minutes)
 * @param maximumSize Maximum number of cache entries (default: 10000)
 */
class CachedKeyValueStoreContextProvider(
    private val delegate: KeyValueStoreContextProvider,
    private val expireAfterWriteMinutes: Long = 5,
    private val maximumSize: Long = 10000,
) : KeyValueStoreContextProvider {

    private val cacheBuilder: Caffeine<Any, Any> = Caffeine.newBuilder()
        .maximumSize(maximumSize)
        .expireAfterWrite(expireAfterWriteMinutes, TimeUnit.MINUTES)

    private val cache: Cache<UUID, List<Pair<String, String>>> = cacheBuilder.build()

    override fun registerContext(id: UUID, key: String, value: String) {
        delegate.registerContext(id, key, value)
        cache.invalidate(id)
    }

    override fun unregisterContext(id: UUID, key: String) {
        delegate.unregisterContext(id, key)
        cache.invalidate(id)
    }

    override fun getContexts(id: UUID): List<Pair<String, String>> {
        return cache.get(id) { delegate.getContexts(id) }
    }

    override fun clearContexts(id: UUID) {
        delegate.clearContexts(id)
        cache.invalidate(id)
    }

    /**
     * Get cache statistics for monitoring.
     */
    fun getCacheStats(): CacheStats {
        return CacheStats(
            contextsCacheSize = cache.estimatedSize(),
        )
    }

    data class CacheStats(
        val contextsCacheSize: Long,
    )
}

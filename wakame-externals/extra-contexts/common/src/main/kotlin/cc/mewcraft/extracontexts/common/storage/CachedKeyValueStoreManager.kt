package cc.mewcraft.extracontexts.common.storage

import cc.mewcraft.extracontexts.api.KeyValueStoreManager
import cc.mewcraft.extracontexts.common.messaging.MessagingManager
import cc.mewcraft.extracontexts.common.messaging.packet.CacheInvalidationPacket
import cc.mewcraft.messaging2.ServerInfoProvider
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Cached wrapper for KeyValueStoreManager using Caffeine cache.
 *
 * Provides an in-memory cache layer using Caffeine to improve performance by reducing database queries.
 * The cache is organized by player UUID and includes individual key lookups and full player data.
 *
 * @param delegate The underlying KeyValueStoreManager implementation
 * @param expireAfterWriteMinutes Cache expiration time in minutes (default: 5 minutes)
 * @param maximumSize Maximum number of cache entries (default: 10000)
 */
class CachedKeyValueStoreManager(
    private val delegate: KeyValueStoreManager,
    expireAfterWriteMinutes: Long = 5,
    maximumSize: Long = 10000,
) : KeyValueStoreManager {

    private val cacheBuilder: Caffeine<Any, Any> = Caffeine.newBuilder()
        .maximumSize(maximumSize)
        .expireAfterWrite(expireAfterWriteMinutes, TimeUnit.MINUTES)

    // Cache for individual key-value pairs: (playerId, key) -> value
    private val keyValueCache: Cache<Pair<UUID, String>, String?> = cacheBuilder.build()

    // Cache for all key-value pairs for a player: playerId -> List<Pair<key, value>>
    private val allKeysCache: Cache<UUID, List<Pair<String, String>>> = cacheBuilder.build()

    // Cache for prefix queries: (playerId, prefix) -> List<Pair<key, value>>
    private val prefixCache: Cache<Pair<UUID, String>, List<Pair<String, String>>> = cacheBuilder.build()

    // Cache for key existence: (playerId, key) -> Boolean
    private val existsCache: Cache<Pair<UUID, String>, Boolean> = cacheBuilder.build()

    override fun get(id: UUID): List<Pair<String, String>> {
        return allKeysCache.get(id) { delegate.get(id) }
    }

    override fun get(id: UUID, key: String): String? {
        val cacheKey = Pair(id, key)
        return keyValueCache.get(cacheKey) { delegate.get(id, key) }
    }

    override fun getWithPrefix(id: UUID, prefix: String): List<Pair<String, String>> {
        val cacheKey = Pair(id, prefix)
        return prefixCache.get(cacheKey) { delegate.getWithPrefix(id, prefix) }
    }

    override fun set(id: UUID, key: String, value: String) {
        delegate.set(id, key, value)
        invalidatePlayerData(id, key)
        MessagingManager.queuePacketAndFlush {
            CacheInvalidationPacket(
                ServerInfoProvider.serverId,
                id,
                CacheInvalidationPacket.InvalidationType.SINGLE_KEY,
                listOf(key)
            )
        }
    }

    override fun delete(id: UUID) {
        delegate.delete(id)
        invalidateAllForPlayer(id)
        MessagingManager.queuePacketAndFlush {
            CacheInvalidationPacket(
                ServerInfoProvider.serverId,
                id,
                CacheInvalidationPacket.InvalidationType.ALL,
                emptyList()
            )
        }
    }

    override fun delete(id: UUID, key: String) {
        delegate.delete(id, key)
        invalidatePlayerData(id, key)
        MessagingManager.queuePacketAndFlush {
            CacheInvalidationPacket(
                ServerInfoProvider.serverId,
                id,
                CacheInvalidationPacket.InvalidationType.SINGLE_KEY,
                listOf(key)
            )
        }
    }

    override fun deleteWithPrefix(id: UUID, prefix: String) {
        delegate.deleteWithPrefix(id, prefix)
        invalidateAllForPlayer(id)
        MessagingManager.queuePacketAndFlush {
            CacheInvalidationPacket(
                ServerInfoProvider.serverId,
                id,
                CacheInvalidationPacket.InvalidationType.PREFIX,
                listOf(prefix)
            )
        }
    }

    override fun exists(id: UUID, key: String): Boolean {
        val cacheKey = Pair(id, key)
        return existsCache.get(cacheKey) { delegate.exists(id, key) }
    }

    override fun clear() {
        delegate.clear()
        keyValueCache.invalidateAll()
        allKeysCache.invalidateAll()
        prefixCache.invalidateAll()
        existsCache.invalidateAll()
    }

    /**
     * Invalidate cache entries for a specific player and key.
     */
    private fun invalidatePlayerData(id: UUID, key: String) {
        keyValueCache.invalidate(Pair(id, key))
        existsCache.invalidate(Pair(id, key))
        allKeysCache.invalidate(id)
        prefixCache.invalidateAll()
    }

    /**
     * Invalidate all cache entries for a specific player.
     */
    private fun invalidateAllForPlayer(id: UUID) {
        allKeysCache.invalidate(id)
        keyValueCache.invalidateAll()
        existsCache.invalidateAll()
        prefixCache.invalidateAll()
    }

    /**
     * Get cache statistics for monitoring.
     */
    fun getCacheStats(): CacheStats {
        return CacheStats(
            keyValueCacheSize = keyValueCache.estimatedSize(),
            allKeysCacheSize = allKeysCache.estimatedSize(),
            prefixCacheSize = prefixCache.estimatedSize(),
            existsCacheSize = existsCache.estimatedSize(),
        )
    }

    data class CacheStats(
        val keyValueCacheSize: Long,
        val allKeysCacheSize: Long,
        val prefixCacheSize: Long,
        val existsCacheSize: Long,
    )
}

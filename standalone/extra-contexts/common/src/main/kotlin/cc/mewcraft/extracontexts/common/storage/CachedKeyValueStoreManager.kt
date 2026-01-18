package cc.mewcraft.extracontexts.common.storage

import cc.mewcraft.extracontexts.api.KeyValueStoreManager
import cc.mewcraft.extracontexts.common.messaging.MessagingManager
import cc.mewcraft.extracontexts.common.messaging.packet.CacheInvalidationPacket
import cc.mewcraft.extracontexts.common.storage.CachedKeyValueStoreManager.delegate
import cc.mewcraft.messaging2.ServerInfoProvider
import com.github.benmanes.caffeine.cache.Caffeine
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Cached wrapper for [KeyValueStoreManager] using Caffeine cache.
 *
 * Provides an in-memory cache layer using Caffeine to improve performance by reducing database queries.
 * The cache is organized by player UUID and includes individual key lookups and full player data.
 *
 * @param delegate The underlying [KeyValueStoreManager] implementation
 */
object CachedKeyValueStoreManager : KeyValueStoreManager {

    private val delegate: KeyValueStoreManager
        get() = SimpleKeyValueStoreManager

    private val cacheBuilder: Caffeine<Any, Any> = Caffeine.newBuilder()
        .maximumSize(10000)
        .expireAfterWrite(5, TimeUnit.MINUTES)

    // Cache for all key-value pairs for a player: playerId -> List<Pair<key, value>>
    private val allKvCache = cacheBuilder.build<UUID, List<Pair<String, String>>>()

    // Cache for prefix queries: (playerId, prefix) -> List<Pair<key, value>>
    private val prefixCache = cacheBuilder.build<Pair<UUID, String>, List<Pair<String, String>>>()

    // Cache for individual key-value pairs: (playerId, key) -> value
    private val singleCache = cacheBuilder.build<Pair<UUID, String>, String?>()

    override fun get(id: UUID): List<Pair<String, String>> {
        return allKvCache.get(id) {
            delegate.get(id)
        }
    }

    override fun get(id: UUID, key: String): String? {
        return singleCache.get(Pair(id, key)) {
            delegate.get(id, key)
        }
    }

    override fun getWithPrefix(id: UUID, prefix: String): List<Pair<String, String>> {
        return prefixCache.get(Pair(id, prefix)) {
            delegate.getWithPrefix(id, prefix)
        }
    }

    override fun set(id: UUID, key: String, value: String) {
        delegate.set(id, key, value)
        invalidateForPlayer(id)
        MessagingManager.queuePacketAndFlush {
            CacheInvalidationPacket(
                ServerInfoProvider.serverId,
                id,
                CacheInvalidationPacket.InvalidationType.SINGLE,
                listOf(key)
            )
        }
    }

    override fun delete(id: UUID) {
        delegate.delete(id)
        invalidateForPlayer(id)
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
        invalidateForPlayer(id)
        MessagingManager.queuePacketAndFlush {
            CacheInvalidationPacket(
                ServerInfoProvider.serverId,
                id,
                CacheInvalidationPacket.InvalidationType.SINGLE,
                listOf(key)
            )
        }
    }

    override fun deleteWithPrefix(id: UUID, prefix: String) {
        delegate.deleteWithPrefix(id, prefix)
        invalidateForPlayer(id)
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
        return get(id, key) != null
    }

    override fun clear() {
        delegate.clear()
        allKvCache.invalidateAll()
        prefixCache.invalidateAll()
        singleCache.invalidateAll()
    }

    /**
     * Invalidate all cache entries for a specific player.
     */
    fun invalidateForPlayer(id: UUID) {
        allKvCache.invalidate(id)
        prefixCache.invalidateAll()
        singleCache.invalidateAll()
    }

    /**
     * Get cache statistics for monitoring.
     */
    fun getCacheStats(): CacheStats {
        return CacheStats(
            keyValueCacheSize = singleCache.estimatedSize(),
            allKeysCacheSize = allKvCache.estimatedSize(),
            prefixCacheSize = prefixCache.estimatedSize(),
        )
    }

    data class CacheStats(
        val keyValueCacheSize: Long,
        val allKeysCacheSize: Long,
        val prefixCacheSize: Long,
    )
}

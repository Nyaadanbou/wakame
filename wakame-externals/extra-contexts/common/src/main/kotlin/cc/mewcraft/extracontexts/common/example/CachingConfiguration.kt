package cc.mewcraft.extracontexts.common.example

import cc.mewcraft.extracontexts.api.KeyValueStoreContextProvider
import cc.mewcraft.extracontexts.api.KeyValueStoreManager
import cc.mewcraft.extracontexts.common.CachingFactory

/**
 * Example configuration for ExtraContexts with caching.
 *
 * This demonstrates how to set up and use cached storage implementations.
 */
object CachingConfiguration {

    /**
     * Example 1: Setup with default caching parameters
     */
    fun setupDefaultCaching() {
        // Create cached implementations using factory
        val cachedManager = CachingFactory.createCachedKeyValueStoreManager()
        val cachedProvider = CachingFactory.createCachedContextProvider()

        // Set as global implementations
        KeyValueStoreManager.setImplementation(cachedManager)
        KeyValueStoreContextProvider.setImplementation(cachedProvider)

        println("✓ Default caching configured")
    }

    /**
     * Example 2: Setup with custom caching parameters for different server sizes
     */
    fun setupForServerSize(serverSize: ServerSize) {
        val (expireMinutes, maxSize) = when (serverSize) {
            ServerSize.SMALL -> 5L to 5000L           // < 100 players
            ServerSize.MEDIUM -> 10L to 15000L        // 100-500 players
            ServerSize.LARGE -> 15L to 30000L         // > 500 players
        }

        val cachedManager = CachingFactory.createCachedKeyValueStoreManager(
            expireAfterWriteMinutes = expireMinutes,
            maximumSize = maxSize
        )
        val cachedProvider = CachingFactory.createCachedContextProvider(
            expireAfterWriteMinutes = expireMinutes,
            maximumSize = maxSize
        )

        KeyValueStoreManager.setImplementation(cachedManager)
        KeyValueStoreContextProvider.setImplementation(cachedProvider)

        println("✓ Caching configured for $serverSize server")
        println("  - Expire after: $expireMinutes minutes")
        println("  - Max cache size: $maxSize entries")
    }

    /**
     * Example 3: Setup with custom configuration
     */
    fun setupCustom(expireMinutes: Long, maxSize: Long, enableProfiling: Boolean = false) {
        val cachedManager = CachingFactory.createCachedKeyValueStoreManager(
            expireAfterWriteMinutes = expireMinutes,
            maximumSize = maxSize
        )
        val cachedProvider = CachingFactory.createCachedContextProvider(
            expireAfterWriteMinutes = expireMinutes,
            maximumSize = maxSize
        )

        KeyValueStoreManager.setImplementation(cachedManager)
        KeyValueStoreContextProvider.setImplementation(cachedProvider)

        if (enableProfiling && cachedManager is cc.mewcraft.extracontexts.common.storage.CachedKeyValueStoreManager) {
            // Print cache statistics periodically
            val stats = cachedManager.getCacheStats()
            println("✓ Caching configured with profiling enabled")
            println("  - Initial cache stats: $stats")
        }

        println("✓ Custom caching configured")
    }

    /**
     * Example 4: Disable caching (use uncached implementations)
     */
    fun disableCaching() {
        // Use original implementations without caching
        val manager = cc.mewcraft.extracontexts.common.storage.SimpleKeyValueStoreManager
        val provider = cc.mewcraft.extracontexts.common.context.SimpleKeyValueStoreContextProvider()

        KeyValueStoreManager.setImplementation(manager)
        KeyValueStoreContextProvider.setImplementation(provider)

        println("✓ Caching disabled - using uncached implementations")
    }

    enum class ServerSize {
        SMALL,
        MEDIUM,
        LARGE,
    }
}

/**
 * Example usage in main plugin initialization:
 *
 * ```kotlin
 * override fun onLoad() {
 *     // Setup caching based on configuration
 *     val serverSize = determineServerSize() // Your logic to determine server size
 *     CachingConfiguration.setupForServerSize(serverSize)
 * }
 * ```
 */


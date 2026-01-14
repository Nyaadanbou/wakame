package cc.mewcraft.extracontexts.common.messaging.handler

import cc.mewcraft.extracontexts.common.messaging.packet.CacheInvalidationPacket
import cc.mewcraft.extracontexts.common.storage.CachedKeyValueStoreManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Process cache invalidation messages.
 */
object MessagingHandler {

    private val logger: Logger = LoggerFactory.getLogger(MessagingHandler::class.java)

    fun handle(packet: CacheInvalidationPacket) {
        when (packet.type) {
            CacheInvalidationPacket.InvalidationType.SINGLE -> {
                packet.data.forEach { key -> logger.info("Invalidating cache for player {}, key: {}", packet.playerId, key) }
                CachedKeyValueStoreManager.invalidateForPlayer(packet.playerId)
            }

            CacheInvalidationPacket.InvalidationType.PREFIX -> {
                packet.data.forEach { prefix -> logger.info("Invalidating cache for player {} with prefix: {}", packet.playerId, prefix) }
                CachedKeyValueStoreManager.invalidateForPlayer(packet.playerId)
            }

            CacheInvalidationPacket.InvalidationType.ALL -> {
                logger.info("Invalidating all cache for player {}", packet.playerId)
                CachedKeyValueStoreManager.invalidateForPlayer(packet.playerId)
            }
        }
    }
}


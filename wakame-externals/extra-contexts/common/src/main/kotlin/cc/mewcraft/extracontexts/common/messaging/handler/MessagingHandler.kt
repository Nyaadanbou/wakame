package cc.mewcraft.extracontexts.common.messaging.handler

import cc.mewcraft.extracontexts.common.messaging.packet.CacheInvalidationPacket
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Process cache invalidation messages.
 */
object MessagingHandler {

    private val logger: Logger = LoggerFactory.getLogger(MessagingHandler::class.java)

    fun handle(packet: CacheInvalidationPacket) {
        when (packet.type) {
            CacheInvalidationPacket.InvalidationType.SINGLE_KEY -> {
                packet.keys.forEach { key ->
                    logger.debug("Invalidating cache for player {}, key: {}", packet.playerId, key)
                }
            }

            CacheInvalidationPacket.InvalidationType.PREFIX -> {
                packet.keys.forEach { prefix ->
                    logger.debug("Invalidating cache for player {} with prefix: {}", packet.playerId, prefix)
                }
            }

            CacheInvalidationPacket.InvalidationType.ALL -> {
                logger.debug("Invalidating all cache for player {}", packet.playerId)
            }
        }
    }
}


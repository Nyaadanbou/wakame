package cc.mewcraft.extracontexts.common.messaging

import cc.mewcraft.extracontexts.common.messaging.handler.MessagingHandler
import cc.mewcraft.extracontexts.common.messaging.packet.CacheInvalidationPacket
import ninja.egg82.messenger.handler.AbstractMessagingHandler
import ninja.egg82.messenger.packets.Packet
import ninja.egg82.messenger.services.PacketService

/**
 * Messaging bus for Extra-Contexts, handling cross-server cache invalidation messages.
 */
class MessagingHandler(
    packetService: PacketService,
) : AbstractMessagingHandler(packetService) {

    override fun handlePacket(packet: Packet): Boolean {
        when (packet) {

            is CacheInvalidationPacket -> {
                MessagingHandler.handle(packet); return true
            }

        }

        return false
    }
}


package cc.mewcraft.wakame.messaging

import cc.mewcraft.wakame.messaging.handler.TeleportOnJoinPacketHandler
import cc.mewcraft.wakame.messaging.handler.TownyNetworkPacketHandler
import cc.mewcraft.wakame.messaging.packet.*
import ninja.egg82.messenger.handler.AbstractMessagingHandler
import ninja.egg82.messenger.packets.Packet
import ninja.egg82.messenger.services.PacketService


/**
 * 总线.
 */
class MessagingHandler(
    packetService: PacketService,
) : AbstractMessagingHandler(packetService) {

    override fun handlePacket(packet: Packet): Boolean {
        when (packet) {

            /* TeleportOnJoin */

            is TeleportOnJoinRequestPacket -> {
                TeleportOnJoinPacketHandler.handle(packet); return true
            }

            /* TownyNetwork */

            is TownSpawnRequestPacket -> {
                TownyNetworkPacketHandler.handle(packet); return true
            }

            is TownSpawnResponsePacket -> {
                TownyNetworkPacketHandler.handle(packet); return true
            }

            is NationSpawnRequestPacket -> {
                TownyNetworkPacketHandler.handle(packet); return true
            }

            is NationSpawnResponsePacket -> {
                TownyNetworkPacketHandler.handle(packet); return true
            }
        }

        return false
    }
}
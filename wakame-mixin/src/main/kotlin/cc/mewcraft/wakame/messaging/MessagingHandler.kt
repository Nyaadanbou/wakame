package cc.mewcraft.wakame.messaging

import cc.mewcraft.wakame.messaging.handler.JEICompatPacketHandler
import cc.mewcraft.wakame.messaging.handler.TeleportOnJoinPacketHandler
import cc.mewcraft.wakame.messaging.handler.TownyBridgeNetworkPacketHandler
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
            /* JEI Compat */

            is JEICompatSyncPacket -> {
                JEICompatPacketHandler.handle(packet); return true
            }

            /* TeleportOnJoin */

            is TeleportOnJoinRequestPacket -> {
                TeleportOnJoinPacketHandler.handle(packet); return true
            }

            /* TownyNetwork */

            is TownSpawnRequestPacket -> {
                TownyBridgeNetworkPacketHandler.handle(packet); return true
            }

            is TownSpawnResponsePacket -> {
                TownyBridgeNetworkPacketHandler.handle(packet); return true
            }

            is NationSpawnRequestPacket -> {
                TownyBridgeNetworkPacketHandler.handle(packet); return true
            }

            is NationSpawnResponsePacket -> {
                TownyBridgeNetworkPacketHandler.handle(packet); return true
            }
        }

        return false
    }
}
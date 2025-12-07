package cc.mewcraft.wakame.messaging

import cc.mewcraft.wakame.messaging.packet.*
import ninja.egg82.messenger.handler.AbstractMessagingHandler
import ninja.egg82.messenger.packets.Packet
import ninja.egg82.messenger.services.PacketService
import java.util.*

class KoishPacketHandler(
    private val serverId: UUID,
    packetService: PacketService,
) : AbstractMessagingHandler(packetService) {

    override fun handlePacket(packet: Packet): Boolean {
        when (packet) {

            // Towny

            is TownSpawnRequestPacket -> {
                TownyNetworkHandler.handle(packet)
                return true
            }

            is TownSpawnResponsePacket -> {
                TownyNetworkHandler.handle(packet)
                return true
            }

            is NationSpawnRequestPacket -> {
                TownyNetworkHandler.handle(packet)
                return true
            }

            is NationSpawnResponsePacket -> {
                TownyNetworkHandler.handle(packet)
                return true
            }

            // ???

            // is ... -> {
            //     return true
            // }
        }
        return false
    }
}
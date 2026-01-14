package cc.mewcraft.wakame.messaging

import cc.mewcraft.wakame.messaging.handler.TownyNetworkPacketHandler
import cc.mewcraft.wakame.messaging.packet.NationSpawnRequestPacket
import cc.mewcraft.wakame.messaging.packet.NationSpawnResponsePacket
import cc.mewcraft.wakame.messaging.packet.TownSpawnRequestPacket
import cc.mewcraft.wakame.messaging.packet.TownSpawnResponsePacket
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

            /* Towny */

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

            // 后续添加更多封包类型时, 在这里添加处理的逻辑

            /* ... */
        }

        return false
    }
}
package cc.mewcraft.wakame.network.event.clientbound

import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket
import org.bukkit.entity.Player

class ClientboundLevelChunkWithLightPacketEvent(
    player: Player,
    packet: ClientboundLevelChunkWithLightPacket
) : PlayerPacketEvent<ClientboundLevelChunkWithLightPacket>(player, packet) {
    
    val x = packet.x
    val z = packet.z
    val chunkData = packet.chunkData
    val lightData = packet.lightData
    
}
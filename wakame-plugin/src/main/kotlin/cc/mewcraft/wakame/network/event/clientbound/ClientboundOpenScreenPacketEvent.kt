package cc.mewcraft.wakame.network.event.clientbound

import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import org.bukkit.entity.Player

class ClientboundOpenScreenPacketEvent(
    player: Player,
    packet: ClientboundOpenScreenPacket
) : PlayerPacketEvent<ClientboundOpenScreenPacket>(player, packet) {
    
    var containerId = packet.containerId
        set(value) {
            field = value
            changed = true
        }
    
    var type = packet.type
        set(value) {
            field = value
            changed = true
        }
    
    var title = packet.title
        set(value) {
            field = value
            changed = true
        }
    
    override fun buildChangedPacket(): ClientboundOpenScreenPacket {
        return ClientboundOpenScreenPacket(containerId, type, title)
    }
    
}
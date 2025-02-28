package cc.mewcraft.wakame.network.event.clientbound

import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import org.bukkit.entity.Player

class ClientboundSetEntityDataPacketEvent(
    player: Player,
    packet: ClientboundSetEntityDataPacket
) : PlayerPacketEvent<ClientboundSetEntityDataPacket>(player, packet) {
    
    var id = packet.id
        set(value) {
            field = value
            changed = true
        }
    var packedItems = packet.packedItems
        set(value) {
            field = value
            changed = true
        }
    
    override fun buildChangedPacket(): ClientboundSetEntityDataPacket {
        return ClientboundSetEntityDataPacket(id, packedItems)
    }
    
}
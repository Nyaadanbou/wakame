package cc.mewcraft.wakame.network.event.clientbound

import cc.mewcraft.wakame.network.ClientboundUpdateAttributesPacket
import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket.AttributeSnapshot
import org.bukkit.entity.Player

class ClientboundUpdateAttributesPacketEvent(
    player: Player,
    packet: ClientboundUpdateAttributesPacket
) : PlayerPacketEvent<ClientboundUpdateAttributesPacket>(player, packet) {
    
    var entityId: Int = packet.entityId
        set(value) {
            field = value
            changed = true
        }
    var values: List<AttributeSnapshot> = packet.values
        set(value) {
            field = value
            changed = true
        }
    
    override fun buildChangedPacket(): ClientboundUpdateAttributesPacket {
        return ClientboundUpdateAttributesPacket(entityId, values)
    }
    
}
package cc.mewcraft.wakame.network.event.clientbound

import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import org.bukkit.entity.Player

class ClientboundContainerSetSlotPacketEvent(
    player: Player,
    packet: ClientboundContainerSetSlotPacket
) : PlayerPacketEvent<ClientboundContainerSetSlotPacket>(player, packet) {
    
    var containerId = packet.containerId
        set(value) {
            field = value
            changed = true
        }
    var stateId = packet.stateId
        set(value) {
            field = value
            changed = true
        }
    var slot = packet.slot
        set(value) {
            field = value
            changed = true
        }
    var item = packet.item
        set(value) {
            field = value
            changed = true
        }
    
    override fun buildChangedPacket(): ClientboundContainerSetSlotPacket {
        return ClientboundContainerSetSlotPacket(
            containerId,
            stateId,
            slot,
            item
        )
    }
    
}
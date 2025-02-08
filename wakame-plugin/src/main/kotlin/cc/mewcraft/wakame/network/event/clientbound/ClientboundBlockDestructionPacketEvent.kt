package cc.mewcraft.wakame.network.event.clientbound

import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket
import org.bukkit.entity.Player

class ClientboundBlockDestructionPacketEvent(
    player: Player,
    packet: ClientboundBlockDestructionPacket
) : PlayerPacketEvent<ClientboundBlockDestructionPacket>(player, packet) {
    
    var entityId = packet.id
        set(value) {
            field = value
            changed = true
        }
    var pos = packet.pos
        set(value) {
            field = value
            changed = true
        }
    var progress = packet.progress
        set(value) {
            field = value
            changed = true
        }
    
    override fun buildChangedPacket(): ClientboundBlockDestructionPacket {
        return ClientboundBlockDestructionPacket(entityId, pos, progress)
    }
    
}
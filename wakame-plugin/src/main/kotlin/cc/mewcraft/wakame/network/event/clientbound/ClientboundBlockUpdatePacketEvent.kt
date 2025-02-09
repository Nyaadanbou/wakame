package cc.mewcraft.wakame.network.event.clientbound

import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import org.bukkit.entity.Player

class ClientboundBlockUpdatePacketEvent(
    player: Player,
    packet: ClientboundBlockUpdatePacket
) : PlayerPacketEvent<ClientboundBlockUpdatePacket>(player, packet) {
    
    var pos = packet.pos
        set(value) {
            field = value
            changed = true
        }
    var blockState = packet.blockState
        set(value) {
            field = value
            changed = true
        }
    
    override fun buildChangedPacket(): ClientboundBlockUpdatePacket {
        return ClientboundBlockUpdatePacket(pos, blockState)
    }
    
}
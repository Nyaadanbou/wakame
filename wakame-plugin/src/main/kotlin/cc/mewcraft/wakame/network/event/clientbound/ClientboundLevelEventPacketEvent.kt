package cc.mewcraft.wakame.network.event.clientbound

import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket
import org.bukkit.entity.Player

class ClientboundLevelEventPacketEvent(
    player: Player,
    packet: ClientboundLevelEventPacket
) : PlayerPacketEvent<ClientboundLevelEventPacket>(player, packet) {
    
    var type: Int = packet.type
        set(value) {
            field = value
            changed = true
        }
    var pos: BlockPos = packet.pos
        set(value) {
            field = value
            changed = true
        }
    var data: Int = packet.data
        set(value) {
            field = value
            changed = true
        }
    var isGlobalEvent: Boolean = packet.isGlobalEvent
        set(value) {
            field = value
            changed = true
        }
    
    override fun buildChangedPacket(): ClientboundLevelEventPacket {
        return ClientboundLevelEventPacket(type, pos, data, isGlobalEvent)
    }
    
}
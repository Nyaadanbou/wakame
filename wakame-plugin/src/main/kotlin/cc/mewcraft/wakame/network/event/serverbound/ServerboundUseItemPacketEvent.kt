package cc.mewcraft.wakame.network.event.serverbound

import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import net.minecraft.network.protocol.game.ServerboundUseItemPacket
import org.bukkit.entity.Player

class ServerboundUseItemPacketEvent(
    player: Player,
    packet: ServerboundUseItemPacket
) : PlayerPacketEvent<ServerboundUseItemPacket>(player, packet) {
    
    var hand = packet.hand
        set(value) {
            field = value
            changed = true
        }
    var sequence = packet.sequence
        set(value) {
            field = value
            changed = true
        }
    var xRot = packet.xRot
        set(value) {
            field = value
            changed = true
        }
    var yRot = packet.yRot
        set(value) {
            field = value
            changed = true
        }
    
    override fun buildChangedPacket(): ServerboundUseItemPacket {
        return ServerboundUseItemPacket(hand, sequence, xRot, yRot)
    }
    
}
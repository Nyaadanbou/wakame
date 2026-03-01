package cc.mewcraft.wakame.network.event.clientbound

import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import org.bukkit.entity.Player

class ClientboundBlockEntityDataPacketEvent(
    player: Player,
    packet: ClientboundBlockEntityDataPacket,
) : PlayerPacketEvent<ClientboundBlockEntityDataPacket>(player, packet) {

    var blockPos = packet.pos
        set(value) {
            field = value
            changed = true
        }

    var type = packet.type
        set(value) {
            field = value
            changed = true
        }

    var tag = packet.tag
        set(value) {
            field = value
            changed = true
        }

    override fun buildChangedPacket(): ClientboundBlockEntityDataPacket {
        return ClientboundBlockEntityDataPacket(blockPos, type, tag)
    }
}
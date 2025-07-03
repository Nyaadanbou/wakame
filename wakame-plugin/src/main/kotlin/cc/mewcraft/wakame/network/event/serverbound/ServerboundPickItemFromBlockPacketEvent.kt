package cc.mewcraft.wakame.network.event.serverbound

import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket
import org.bukkit.entity.Player

class ServerboundPickItemFromBlockPacketEvent(
    player: Player,
    packet: ServerboundPickItemFromBlockPacket
) : PlayerPacketEvent<ServerboundPickItemFromBlockPacket>(player, packet) {

    var pos: BlockPos = packet.pos
        set(value) {
            field = value
            changed = true
        }

    var includeData: Boolean = packet.includeData
        set(value) {
            field = value
            changed = true
        }

    override fun buildChangedPacket(): ServerboundPickItemFromBlockPacket {
        return ServerboundPickItemFromBlockPacket(pos, includeData)
    }

}
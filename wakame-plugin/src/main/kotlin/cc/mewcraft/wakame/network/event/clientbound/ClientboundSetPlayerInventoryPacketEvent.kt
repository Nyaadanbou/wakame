package cc.mewcraft.wakame.network.event.clientbound

import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket
import org.bukkit.entity.Player

class ClientboundSetPlayerInventoryPacketEvent(
    player: Player,
    packet: ClientboundSetPlayerInventoryPacket,
) : PlayerPacketEvent<ClientboundSetPlayerInventoryPacket>(player, packet) {

    var slot = packet.slot
        set(value) {
            field = value
            changed = true
        }

    var contents = packet.contents
        set(value) {
            field = value
            changed = true
        }

    override fun buildChangedPacket(): ClientboundSetPlayerInventoryPacket {
        return ClientboundSetPlayerInventoryPacket(slot, contents)
    }
}
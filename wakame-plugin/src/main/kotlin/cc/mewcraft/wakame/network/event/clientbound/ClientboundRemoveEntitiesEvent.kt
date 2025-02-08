package cc.mewcraft.wakame.network.event.clientbound

import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import org.bukkit.entity.Player

class ClientboundRemoveEntitiesEvent(
    player: Player,
    packet: ClientboundRemoveEntitiesPacket
) : PlayerPacketEvent<ClientboundRemoveEntitiesPacket>(player, packet) {
    var entityIds = packet.entityIds
        set(value) {
            field = value
            changed = true
        }

    override fun buildChangedPacket(): ClientboundRemoveEntitiesPacket {
        return ClientboundRemoveEntitiesPacket(entityIds)
    }
}

package cc.mewcraft.wakame.network.event.clientbound

import cc.mewcraft.wakame.network.ClientboundSetPassengersPacket
import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket
import org.bukkit.entity.Player

class ClientboundSetPassengersPacketEvent(
    player: Player,
    packet: ClientboundSetPassengersPacket
) : PlayerPacketEvent<ClientboundSetPassengersPacket>(player, packet) {
    
    var vehicle = packet.vehicle
        set(value) {
            field = value
            changed = true
        }
    var passengers = packet.passengers
        set(value) {
            field = value
            changed = true
        }
    
    override fun buildChangedPacket(): ClientboundSetPassengersPacket {
        return ClientboundSetPassengersPacket(vehicle, passengers)
    }
    
}   
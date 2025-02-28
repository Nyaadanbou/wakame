package cc.mewcraft.wakame.network.event.clientbound

import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import net.minecraft.network.protocol.game.ClientboundRecipeBookAddPacket
import org.bukkit.entity.Player

class ClientboundRecipeBookAddPacketEvent(
    player: Player,
    packet: ClientboundRecipeBookAddPacket
) : PlayerPacketEvent<ClientboundRecipeBookAddPacket>(player, packet) {
    
    var entries: List<ClientboundRecipeBookAddPacket.Entry> = packet.entries
        set(value) {
            field = value
            changed = true
        }
    var replace: Boolean = packet.replace
        set(value) {
            field = value
            changed = true
        }
    
    override fun buildChangedPacket(): ClientboundRecipeBookAddPacket {
        return ClientboundRecipeBookAddPacket(entries, replace)
    }
    
}
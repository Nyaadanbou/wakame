package cc.mewcraft.wakame.network.event.clientbound

import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import cc.mewcraft.wakame.util.component.adventure.toAdventureComponent
import cc.mewcraft.wakame.util.component.adventure.toNMSComponent
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket
import org.bukkit.entity.Player

class ClientboundActionBarPacketEvent(
    player: Player,
    packet: ClientboundSetActionBarTextPacket
) : PlayerPacketEvent<ClientboundSetActionBarTextPacket>(player, packet) {
    
    var text: Component = packet.text().toAdventureComponent()
        set(value) {
            field = value
            changed = true
        }
    
    override fun buildChangedPacket(): ClientboundSetActionBarTextPacket {
        return ClientboundSetActionBarTextPacket(text.toNMSComponent())
    }
    
}
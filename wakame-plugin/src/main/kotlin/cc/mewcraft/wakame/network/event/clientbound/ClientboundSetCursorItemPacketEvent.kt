package cc.mewcraft.wakame.network.event.clientbound

import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import net.minecraft.network.protocol.game.ClientboundSetCursorItemPacket
import net.minecraft.world.item.ItemStack
import org.bukkit.entity.Player

class ClientboundSetCursorItemPacketEvent(
    player: Player,
    packet: ClientboundSetCursorItemPacket
) : PlayerPacketEvent<ClientboundSetCursorItemPacket>(player, packet) {
    
    var item: ItemStack = packet.contents
        set(value) {
            field = value
            changed = true
        }
    
    override fun buildChangedPacket(): ClientboundSetCursorItemPacket {
        return ClientboundSetCursorItemPacket(item)
    }
    
}
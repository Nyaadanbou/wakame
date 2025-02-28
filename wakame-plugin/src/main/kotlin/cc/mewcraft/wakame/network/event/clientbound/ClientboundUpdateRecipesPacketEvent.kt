package cc.mewcraft.wakame.network.event.clientbound

import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket
import org.bukkit.entity.Player

class ClientboundUpdateRecipesPacketEvent(
    player: Player,
    packet: ClientboundUpdateRecipesPacket
) : PlayerPacketEvent<ClientboundUpdateRecipesPacket>(player, packet) {
    
    var itemSets = packet.itemSets
        set(value) {
            field = value
            changed = true
        }
    
    var stonecutterRecipes = packet.stonecutterRecipes
        set(value) {
            field = value
            changed = true
        }
    
    override fun buildChangedPacket(): ClientboundUpdateRecipesPacket {
        return ClientboundUpdateRecipesPacket(itemSets, stonecutterRecipes)
    }
    
}
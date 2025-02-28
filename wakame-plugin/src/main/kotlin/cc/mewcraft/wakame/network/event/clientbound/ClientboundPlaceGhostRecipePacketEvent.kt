package cc.mewcraft.wakame.network.event.clientbound

import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket
import net.minecraft.world.item.crafting.display.RecipeDisplay
import org.bukkit.entity.Player

class ClientboundPlaceGhostRecipePacketEvent(
    player: Player,
    packet: ClientboundPlaceGhostRecipePacket
) : PlayerPacketEvent<ClientboundPlaceGhostRecipePacket>(player, packet) {
    
    var containerId: Int = packet.containerId
        set(value) {
            field = value
            changed = true
        }
    
    var recipeDisplay: RecipeDisplay = packet.recipeDisplay
        set(value) {
            field = value
            changed = true
        }
    
    override fun buildChangedPacket(): ClientboundPlaceGhostRecipePacket {
        return ClientboundPlaceGhostRecipePacket(containerId, recipeDisplay)
    }
    
}
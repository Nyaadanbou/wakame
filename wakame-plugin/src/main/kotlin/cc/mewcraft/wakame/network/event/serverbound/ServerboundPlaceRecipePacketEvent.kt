package cc.mewcraft.wakame.network.event.serverbound

import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket
import org.bukkit.entity.Player

class ServerboundPlaceRecipePacketEvent(
    player: Player,
    packet: ServerboundPlaceRecipePacket
) : PlayerPacketEvent<ServerboundPlaceRecipePacket>(player, packet) {
    
    var containerId = packet.containerId
        set(value) {
            field = value
            changed = true
        }
    var recipe = packet.recipe
        set(value) {
            field = value
            changed = true
        }
    var useMaxItems = packet.useMaxItems
        set(value) {
            field = value
            changed = true
        }
    
    override fun buildChangedPacket(): ServerboundPlaceRecipePacket {
        return ServerboundPlaceRecipePacket(containerId, recipe, useMaxItems)
    }
    
}
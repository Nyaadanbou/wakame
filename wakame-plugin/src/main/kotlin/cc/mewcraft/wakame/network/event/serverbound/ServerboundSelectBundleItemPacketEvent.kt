package cc.mewcraft.wakame.network.event.serverbound

import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket
import org.bukkit.entity.Player

class ServerboundSelectBundleItemPacketEvent(
    player: Player,
    packet: ServerboundSelectBundleItemPacket
) : PlayerPacketEvent<ServerboundSelectBundleItemPacket>(player, packet) {
    
    var slotId: Int = packet.slotId
        set(value) {
            field = value
            changed = true
        }
    var selectedItemIndex: Int = packet.selectedItemIndex
        set(value) {
            field = value
            changed = true
        }
    
    override fun buildChangedPacket(): ServerboundSelectBundleItemPacket {
        return ServerboundSelectBundleItemPacket(slotId, selectedItemIndex)
    }
    
    
}
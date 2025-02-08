package cc.mewcraft.wakame.network.event.serverbound

import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket
import org.bukkit.entity.Player

class ServerboundSetCreativeModeSlotPacketEvent(
    player: Player,
    packet: ServerboundSetCreativeModeSlotPacket
) : PlayerPacketEvent<ServerboundSetCreativeModeSlotPacket>(player, packet) {
    
    var slotNum = packet.slotNum
        set(value) {
            field = value
            changed = true
        }
    var itemStack = packet.itemStack
        set(value) {
            field = value
            changed = true
        }
    
    override fun buildChangedPacket(): ServerboundSetCreativeModeSlotPacket {
        return ServerboundSetCreativeModeSlotPacket(slotNum, itemStack)
    }
    
}
package cc.mewcraft.wakame.network.event.clientbound

import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import com.mojang.datafixers.util.Pair
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import org.bukkit.entity.Player

class ClientboundSetEquipmentPacketEvent(
    player: Player,
    packet: ClientboundSetEquipmentPacket
) : PlayerPacketEvent<ClientboundSetEquipmentPacket>(player, packet) {
    
    var entity: Int = packet.entity
    var slots: List<Pair<EquipmentSlot, ItemStack>> = packet.slots
        set(value) {
            field = value
            changed = true
        }
    
    override fun buildChangedPacket(): ClientboundSetEquipmentPacket {
        return ClientboundSetEquipmentPacket(
            entity,
            slots
        )
    }
    
}
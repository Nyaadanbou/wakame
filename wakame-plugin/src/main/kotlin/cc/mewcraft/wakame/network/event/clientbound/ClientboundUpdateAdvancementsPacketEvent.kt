package cc.mewcraft.wakame.network.event.clientbound

import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket
import org.bukkit.entity.Player

class ClientboundUpdateAdvancementsPacketEvent(
    player: Player,
    packet: ClientboundUpdateAdvancementsPacket,
) : PlayerPacketEvent<ClientboundUpdateAdvancementsPacket>(player, packet) {

    var reset: Boolean = packet.shouldReset()
        set(value) {
            field = value
            changed = true
        }

    var added = packet.added
        set(value) {
            field = value
            changed = true
        }

    var removed = packet.removed
        set(value) {
            field = value
            changed = true
        }

    var progress = packet.progress
        set(value) {
            field = value
            changed = true
        }

    var showAdvancements = packet.shouldShowAdvancements()
        set(value) {
            field = value
            changed = true
        }

    override fun buildChangedPacket(): ClientboundUpdateAdvancementsPacket {
        return ClientboundUpdateAdvancementsPacket(
            reset,
            added,
            removed,
            progress,
            showAdvancements
        )
    }
}
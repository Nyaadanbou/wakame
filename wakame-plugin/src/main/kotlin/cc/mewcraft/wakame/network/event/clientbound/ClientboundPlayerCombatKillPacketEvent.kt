package cc.mewcraft.wakame.network.event.clientbound

import cc.mewcraft.wakame.network.event.PlayerPacketEvent
import cc.mewcraft.wakame.util.component.adventure.toAdventureComponent
import cc.mewcraft.wakame.util.component.adventure.toNMSComponent
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket
import org.bukkit.entity.Player

class ClientboundPlayerCombatKillPacketEvent(
    player: Player,
    packet: ClientboundPlayerCombatKillPacket
): PlayerPacketEvent<ClientboundPlayerCombatKillPacket>(player, packet) {

    var playerId: Int = packet.playerId
        set(value) {
            field = value
            changed = true
        }

    var message: Component = packet.message.toAdventureComponent()
        set(value) {
            field = value
            changed = true
        }

    override fun buildChangedPacket(): ClientboundPlayerCombatKillPacket {
        return ClientboundPlayerCombatKillPacket(playerId, message.toNMSComponent())
    }
}
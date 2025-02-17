package cc.mewcraft.wakame.network.event.clientbound

import cc.mewcraft.wakame.network.event.PacketEvent
import net.minecraft.core.Registry
import net.minecraft.core.RegistrySynchronization
import net.minecraft.network.protocol.configuration.ClientboundRegistryDataPacket
import net.minecraft.resources.ResourceKey

class ClientboundRegistryDataPacketEvent(
    packet: ClientboundRegistryDataPacket,
) : PacketEvent<ClientboundRegistryDataPacket>(packet) {

    var registry: ResourceKey<out Registry<*>> = packet.registry
        set(value) {
            field = value
            changed = true
        }

    var entries: List<RegistrySynchronization.PackedRegistryEntry> = packet.entries
        set(value) {
            field = value
            changed = true
        }

    override fun buildChangedPacket(): ClientboundRegistryDataPacket {
        return ClientboundRegistryDataPacket(registry, entries)
    }

}
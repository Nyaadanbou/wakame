package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.element.ElementType
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.util.adventure.toSimpleString
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.Collections.emptyMap
import java.util.stream.Stream

/**
 * 创建一个 [DamageBundle].
 */
fun DamageBundle(
    packets: Map<RegistryEntry<ElementType>, DamagePacket> = emptyMap(),
): DamageBundle {
    return DamageBundleImpl(packets)
}

internal object DefaultDamageBundleFactory : DamageBundleFactory {
    override fun createUnsafe(data: Map<String, DamagePacket>): DamageBundle {
        val packets = mutableMapOf<RegistryEntry<ElementType>, DamagePacket>()
        for ((id, packet) in data) {
            val element = getElementById(id)
            if (element != null) {
                packets[element] = packet
            }
        }
        return DamageBundle(packets)
    }

    override fun create(data: Map<RegistryEntry<ElementType>, DamagePacket>): DamageBundle {
        return DamageBundle(data)
    }
}

private fun getElementById(id: String): RegistryEntry<ElementType>? {
    return KoishRegistries.ELEMENT.getEntry(id)
}

private class DamageBundleImpl(
    packets: Map<RegistryEntry<ElementType>, DamagePacket>,
) : DamageBundle, Examinable {
    private val packets = Reference2ObjectArrayMap(packets)

    override fun total(): Double {
        return packets.values.sumOf { it.packetDamage }
    }

    override fun add(packet: DamagePacket) {
        val element = packet.element
        val previous = packets.put(element, packet)
        if (previous != null) {
            LOGGER.warn("Failed to overwrite a packet of the same element type: '${element.getIdAsString()}'")
        }
    }

    override fun addIfAbsent(packet: DamagePacket) {
        packets.putIfAbsent(packet.element, packet)
    }

    override fun remove(element: RegistryEntry<ElementType>): DamagePacket? {
        return packets.remove(element)
    }

    override fun remove(id: String): DamagePacket? {
        val element = getElementById(id) ?: return null
        return packets.remove(element)
    }

    override fun get(element: RegistryEntry<ElementType>): DamagePacket? {
        return packets[element]
    }

    override fun get(id: String): DamagePacket? {
        val element = getElementById(id) ?: return null
        return get(element)
    }

    override fun packets(): Collection<DamagePacket> {
        return packets.values
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("packets", packets)
    )

    override fun toString(): String {
        return toSimpleString()
    }
}
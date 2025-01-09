package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.core.registries.KoishRegistries
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.util.toSimpleString
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * 创建一个 [DamageBundle].
 */
fun DamageBundle(
    packets: Map<Element, DamagePacket> = emptyMap(),
): DamageBundle {
    return DamageBundleImpl(packets)
}

internal object DefaultDamageBundleFactory : DamageBundleFactory {
    override fun createUnsafe(data: Map<String, DamagePacket>): DamageBundle {
        val packets = mutableMapOf<Element, DamagePacket>()
        for ((id, packet) in data) {
            val element = getElementById(id)
            if (element != null) {
                packets[element] = packet
            }
        }
        return DamageBundle(packets)
    }

    override fun create(data: Map<Element, DamagePacket>): DamageBundle {
        return DamageBundle(data)
    }
}

private fun getElementById(id: String): Element? {
    return KoishRegistries.ELEMENT.get(id) ?: run {
        LOGGER.warn("No such element: '$id'")
        return null
    }
}

private class DamageBundleImpl(
    packets: Map<Element, DamagePacket>,
) : DamageBundle, Examinable {
    private val packets = Reference2ObjectArrayMap(packets)

    override fun total(): Double {
        return packets.values.sumOf { it.damageValue() }
    }

    override fun add(packet: DamagePacket) {
        val element = packet.element
        val previous = packets.put(element, packet)
        if (previous != null) {
            LOGGER.warn("Failed to overwrite a packet of the same element type: '${element.stringId}'")
        }
    }

    override fun addIfAbsent(packet: DamagePacket) {
        packets.putIfAbsent(packet.element, packet)
    }

    override fun remove(element: Element): DamagePacket? {
        return packets.remove(element)
    }

    override fun remove(id: String): DamagePacket? {
        val element = getElementById(id) ?: return null
        return packets.remove(element)
    }

    override fun get(element: Element): DamagePacket? {
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
package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.util.toSimpleString
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
import me.lucko.helper.random.VariableAmount
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import java.util.stream.Stream

/**
 * 伤害包，包含了一种特定元素的伤害信息
 */
data class DamagePacket(
    /**
     * 伤害的元素类型
     */
    val element: Element,

    /**
     * 伤害的最小值
     */
    val min: Double,

    /**
     * 伤害的最大值
     */
    val max: Double,

    /**
     * 伤害的加成比率
     * 例如：火元素伤害+50%
     */
    val rate: Double,

    /**
     * 伤害的护甲穿透值
     */
    val defensePenetration: Double,

    /**
     * 伤害的护甲穿透率
     */
    val defensePenetrationRate: Double,
) : Examinable {

    /**
     * 伤害值在最大值与最小值之间的随机结果
     * 伤害值在伤害包实例化时就已经确定了
     */
    val value: Double = if (min >= max) max else VariableAmount.range(min, max).amount

    /**
     * 该伤害包的总伤害值，称为“包伤害”
     * [DamageMetadata] 的伤害值是捆绑包中所有包伤害的和
     */
    val packetDamage: Double = value

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("element", element.uniqueId),
        ExaminableProperty.of("min", min),
        ExaminableProperty.of("max", max),
        ExaminableProperty.of("rate", rate),
        ExaminableProperty.of("defense_penetration", defensePenetration),
        ExaminableProperty.of("defense_penetration_rate", defensePenetrationRate),
        ExaminableProperty.of("value", value),
        ExaminableProperty.of("packet_damage", packetDamage),
    )

    override fun toString(): String {
        return toSimpleString()
    }
}

/**
 * 伤害捆绑包, 封装了一个或多个 [DamagePacket].
 */
class DamageBundle : Examinable, KoinComponent {
    private val logger: Logger by inject()
    private val packets = Reference2ObjectArrayMap<Element, DamagePacket>()
    val bundleDamage: Double = packets.values.sumOf { it.packetDamage }

    private fun getElementById(id: String): Element? {
        return ElementRegistry.INSTANCES.find(id) ?: run {
            logger.warn("Element '$id' not found")
            return null
        }
    }

    /**
     * 向伤害捆绑包中添加元素伤害包, 将覆盖已有的元素伤害包.
     */
    fun add(packet: DamagePacket) {
        val element = packet.element
        val previous = packets.put(element, packet)
        if (previous != null) {
            logger.warn("Overwrote a packet of the same element type: '${element.uniqueId}'")
        }
    }

    /**
     * 向伤害捆绑包中添加元素伤害包, 如果已存在相同元素的伤害包则不添加.
     */
    fun addIfAbsent(packet: DamagePacket) {
        packets.putIfAbsent(packet.element, packet)
    }

    /**
     * 从伤害捆绑包中移除元素伤害包.
     */
    fun remove(element: Element): DamagePacket? {
        return packets.remove(element)
    }

    /**
     * 从伤害捆绑包中移除元素伤害包.
     */
    fun remove(id: String): DamagePacket? {
        val element = getElementById(id) ?: return null
        return packets.remove(element)
    }

    /**
     * 从伤害捆绑包中获取元素伤害包.
     */
    fun get(element: Element): DamagePacket? {
        return packets[element]
    }

    /**
     * 从伤害捆绑包中获取元素伤害包.
     */
    fun get(id: String): DamagePacket? {
        val element = getElementById(id) ?: return null
        return get(element)
    }

    /**
     * 从伤害捆绑包中获取所有元素伤害包.
     */
    fun packets(): Collection<DamagePacket> {
        return packets.values
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("packets", packets)
    )

    override fun toString(): String {
        return toSimpleString()
    }
}
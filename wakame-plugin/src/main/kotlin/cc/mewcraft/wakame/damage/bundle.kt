package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.util.adventure.toSimpleString
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.jetbrains.annotations.ApiStatus
import java.util.Collections.emptyMap
import java.util.stream.Stream
import kotlin.random.Random

/**
 * 创建一个 [DamageBundle].
 */
fun DamageBundle(
    packets: Map<RegistryEntry<Element>, DamagePacket> = emptyMap(),
): DamageBundle {
    return DamageBundleImpl(packets)
}

/**
 * 伤害捆绑包, 包含了一次伤害中的所有元素的[伤害包][DamagePacket].
 */
interface DamageBundle {
    /**
     * 该捆绑包的总伤害值.
     */
    fun total(): Double

    // TODO #366: 分离 API.
    //  1) Mixin 模块中的 DamageBundle 不需要 mutable.
    //  2) ElementType 必须先迁移到 Mixin 才能迁移伤害.
    /**
     * 向伤害捆绑包中添加元素伤害包, 将覆盖已有的元素伤害包.
     */
    fun add(packet: DamagePacket)

    /**
     * 向伤害捆绑包中添加元素伤害包, 如果已存在相同元素的伤害包则不添加.
     */
    fun addIfAbsent(packet: DamagePacket)

    /**
     * 从伤害捆绑包中移除元素伤害包.
     */
    fun remove(element: RegistryEntry<Element>): DamagePacket?

    /**
     * 从伤害捆绑包中移除元素伤害包.
     *
     * @param id 元素的标识
     */
    fun remove(id: String): DamagePacket?

    /**
     * 从伤害捆绑包中获取元素伤害包.
     */
    fun get(element: RegistryEntry<Element>): DamagePacket?

    /**
     * 从伤害捆绑包中获取元素伤害包.
     *
     * @param id 元素的标识
     */
    fun get(id: String): DamagePacket?

    /**
     * 从伤害捆绑包中获取所有元素伤害包.
     */
    fun packets(): Collection<DamagePacket>
}

/**
 * 用于创建 [DamageBundle] 的实例.
 */
interface DamageBundleFactory {

    /**
     * 创建一个 [DamageBundle].
     */
    fun create(data: Map<RegistryEntry<Element>, DamagePacket>): DamageBundle

    /**
     * 创建一个 [DamageBundle].
     * 传入的映射为 `element id` -> `damage packet`.
     * 如果 `element id` 不是有效的元素, 则会记录并忽略该元素.
     */
    fun createUnsafe(data: Map<String, DamagePacket>): DamageBundle

    /**
     * 伴生对象, 提供 [DamageBundleFactory] 的实例.
     */
    companion object Holder : DamageBundleFactory {
        private var instance: DamageBundleFactory? = null

        @JvmStatic
        fun instance(): DamageBundleFactory {
            return instance ?: throw IllegalStateException("DamageBundleFactory has not been initialized")
        }

        @ApiStatus.Internal
        fun register(factory: DamageBundleFactory) {
            instance = factory
        }

        @ApiStatus.Internal
        fun unregister() {
            instance = null
        }

        override fun create(data: Map<RegistryEntry<Element>, DamagePacket>): DamageBundle {
            return instance().create(data)
        }

        override fun createUnsafe(data: Map<String, DamagePacket>): DamageBundle {
            return instance().createUnsafe(data)
        }
    }
}

/**
 * 创建一个 [DamagePacket].
 */
fun DamagePacket(
    elementId: String,
    min: Double,
    max: Double,
    rate: Double = 1.0,
    defensePenetration: Double = .0,
    defensePenetrationRate: Double = .0,
): DamagePacket {
    return DamagePacket(
        element = BuiltInRegistries.ELEMENT.getEntryOrThrow(elementId),
        min = min,
        max = max,
        rate = rate,
        defensePenetration = defensePenetration,
        defensePenetrationRate = defensePenetrationRate
    )
}

/**
 * 伤害包, 包含了一种特定元素的伤害信息.
 */
data class DamagePacket(
    /**
     * 伤害的元素类型.
     */
    val element: RegistryEntry<Element>,

    /**
     * 伤害的最小值.
     */
    val min: Double,

    /**
     * 伤害的最大值.
     */
    val max: Double,

    /**
     * 伤害的加成比率. 例如: 火元素伤害+50%.
     */
    val rate: Double = 1.0,

    /**
     * 伤害的护甲穿透值.
     */
    val defensePenetration: Double = .0,

    /**
     * 伤害的护甲穿透率.
     */
    val defensePenetrationRate: Double = .0,
) {
    /**
     * 该伤害包的伤害值, 称为"包伤害". 初始化时会在 [min] 与 [max] 之间随机.
     */
    val packetDamage: Double = if (min >= max) {
        max
    } else {
        Random.nextDouble(min, max)
    }

    // 检查数据的合法性
    init {
        require(min >= 0.0) { "min damage must be greater than or equal to 0.0" }
        require(max >= 0.0) { "max damage must be greater than or equal to 0.0" }
        require(rate >= 0.0) { "rate must be greater than or equal to 0.0" }
        require(defensePenetration >= 0.0) { "defense penetration must be greater than or equal to 0.0" }
        require(defensePenetrationRate >= 0.0) { "defense penetration rate must be greater than or equal to 0.0" }
    }
}

// ------------
// 内部实现
// ------------

internal object DefaultDamageBundleFactory : DamageBundleFactory {
    override fun createUnsafe(data: Map<String, DamagePacket>): DamageBundle {
        val packets = mutableMapOf<RegistryEntry<Element>, DamagePacket>()
        for ((id, packet) in data) {
            val element = getElementById(id)
            if (element != null) {
                packets[element] = packet
            }
        }
        return DamageBundle(packets)
    }

    override fun create(data: Map<RegistryEntry<Element>, DamagePacket>): DamageBundle {
        return DamageBundle(data)
    }
}

private fun getElementById(id: String): RegistryEntry<Element>? {
    return BuiltInRegistries.ELEMENT.getEntry(id)
}

private class DamageBundleImpl(
    packets: Map<RegistryEntry<Element>, DamagePacket>,
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

    override fun remove(element: RegistryEntry<Element>): DamagePacket? {
        return packets.remove(element)
    }

    override fun remove(id: String): DamagePacket? {
        val element = getElementById(id) ?: return null
        return packets.remove(element)
    }

    override fun get(element: RegistryEntry<Element>): DamagePacket? {
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
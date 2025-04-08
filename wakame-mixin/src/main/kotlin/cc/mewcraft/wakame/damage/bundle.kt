package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import org.jetbrains.annotations.ApiStatus
import kotlin.random.Random


/**
 * 伤害捆绑包, 包含了一次伤害中的所有元素的[伤害包][DamagePacket].
 */
interface DamageBundle {

    companion object {

        /**
         * 创建一个 [DamageBundle].
         */
        fun damageBundleOf(packets: Map<RegistryEntry<Element>, DamagePacket>): DamageBundle {
            return damageBundleOf(packets.values)
        }

        /**
         * 创建一个 [DamageBundle].
         */
        fun damageBundleOf(packets: Collection<DamagePacket>): DamageBundle {
            return SimpleDamageBundle(packets = ObjectArraySet(packets), copyOnWrite = false)
        }

        /**
         * 构建一个 [DamageBundle].
         */
        fun build(block: Builder.() -> Unit): DamageBundle {
            return builder().apply(block).build()
        }

        /**
         * 返回一个新的 [DamageBundle.Builder].
         */
        fun builder(): Builder {
            return SimpleDamageBundle(ObjectArraySet(), copyOnWrite = false)
        }
    }

    /**
     * 该捆绑包的总伤害值.
     */
    fun total(): Double

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

    /**
     * 基于当前状态创建一个新的 [Builder].
     */
    fun toBuilder(): Builder

    /**
     * [DamageBundle] 的生成器.
     */
    interface Builder {

        /**
         * 向伤害捆绑包中添加元素伤害包, 将覆盖已有的元素伤害包.
         */
        fun add(packet: DamagePacket)

        /**
         * 向伤害捆绑包中添加元素伤害包, 如果已存在相同元素的伤害包则不添加.
         */
        fun addIfAbsent(packet: DamagePacket)

        /**
         * 从伤害捆绑包中移除 *首个* [element] 元素的伤害包.
         */
        fun removeFirst(element: RegistryEntry<Element>): DamagePacket?

        /**
         * 从伤害捆绑包中移除元素伤害包.
         *
         * @param id 元素的标识
         */
        fun removeFirst(id: String): DamagePacket?

        /**
         * 从伤害捆绑包中移除 *所有* [element] 元素的伤害包.
         */
        fun remove(element: RegistryEntry<Element>): Boolean

        /**
         * 从伤害捆绑包中移除 *所有* [id] 元素的伤害包.
         */
        fun remove(id: String): Boolean

        /**
         * 构建.
         */
        fun build(): DamageBundle
    }
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
    companion object {
        @get:JvmStatic
        @get:JvmName("getInstance")
        lateinit var INSTANCE: DamageBundleFactory private set

        @ApiStatus.Internal
        fun register(instance: DamageBundleFactory) {
            INSTANCE = instance
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
 *
 * @property element 伤害的元素类型
 * @property min 伤害的最小值
 * @property max 伤害的最大值
 * @property rate 伤害的加成比率, 例如: 火元素伤害+50%
 * @property defensePenetration 伤害的护甲穿透值
 * @property defensePenetrationRate 伤害的护甲穿透率
 */
data class DamagePacket(
    val element: RegistryEntry<Element>,
    val min: Double,
    val max: Double,
    val rate: Double = 1.0,
    val defensePenetration: Double = .0, // abbr. = dp
    val defensePenetrationRate: Double = .0, // abbr. = dpr
) {

    // 检查数据的合法性
    init {
        require(min >= 0.0) { "min damage must be greater than or equal to 0.0" }
        require(max >= 0.0) { "max damage must be greater than or equal to 0.0" }
        require(rate >= 0.0) { "rate must be greater than or equal to 0.0" }
        require(defensePenetration >= 0.0) { "defense penetration must be greater than or equal to 0.0" }
        require(defensePenetrationRate >= 0.0) { "defense penetration rate must be greater than or equal to 0.0" }
    }

    /**
     * 该伤害包的伤害值, 称为"包伤害". 初始化时会在 [min] 与 [max] 之间随机.
     */
    val packetDamage: Double = if (min >= max) {
        max
    } else {
        Random.nextDouble(min, max)
    }

    override fun toString(): String {
        return "${element.getIdAsString()} => {min=$min, max=$max, rate=$rate, dp=$defensePenetration, dpr=$defensePenetrationRate}"
    }
}

// ------------
// 内部实现
// ------------

private class SimpleDamageBundle : DamageBundle, DamageBundle.Builder {

    private var packets: ObjectArraySet<DamagePacket>
    private var copyOnWrite: Boolean

    constructor(packets: ObjectArraySet<DamagePacket>, copyOnWrite: Boolean) {
        this.packets = packets
        this.copyOnWrite = copyOnWrite
    }

    private fun ensureContainerOwnership() {
        if (copyOnWrite) {
            packets = packets.clone()
            copyOnWrite = false
        }
    }

    override fun total(): Double {
        return packets.sumOf { it.packetDamage }
    }

    override fun get(element: RegistryEntry<Element>): DamagePacket? {
        return packets.first { it.element == element }
    }

    override fun get(id: String): DamagePacket? {
        return BuiltInRegistries.ELEMENT.getEntry(id)?.let(::get)
    }

    override fun packets(): Collection<DamagePacket> {
        return packets
    }

    override fun toBuilder(): DamageBundle.Builder {
        copyOnWrite = true
        return SimpleDamageBundle(packets = packets, copyOnWrite = true)
    }

    override fun add(packet: DamagePacket) {
        ensureContainerOwnership()
        val previous = packets.add(packet)
        if (!previous) {
            LOGGER.warn("Failed to overwrite a packet of the same element type: $packet")
        }
    }

    override fun addIfAbsent(packet: DamagePacket) {
        ensureContainerOwnership()
        if (packets.all { it.element != packet.element }) {
            packets.add(packet)
        }
    }

    override fun removeFirst(element: RegistryEntry<Element>): DamagePacket? {
        ensureContainerOwnership()
        val iterator = packets.iterator()
        while (iterator.hasNext()) {
            val packet = iterator.next()
            if (packet.element == element) {
                iterator.remove()
                return packet
            }
        }
        return null
    }

    override fun removeFirst(id: String): DamagePacket? {
        ensureContainerOwnership()
        return BuiltInRegistries.ELEMENT.getEntry(id)?.let(::removeFirst)
    }

    override fun remove(element: RegistryEntry<Element>): Boolean {
        ensureContainerOwnership()
        return packets.removeIf { it.element == element }
    }

    override fun remove(id: String): Boolean {
        ensureContainerOwnership()
        return BuiltInRegistries.ELEMENT.getEntry(id)?.let(::remove) == true
    }

    override fun build(): DamageBundle {
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SimpleDamageBundle) return false
        if (packets != other.packets) return false
        return true
    }

    override fun hashCode(): Int {
        return packets.hashCode()
    }

    override fun toString(): String {
        return "{${
            packets.joinToString(
                separator = ", ",
                prefix = "(",
                postfix = ")",
                transform = DamagePacket::toString
            )
        }}"
    }
}
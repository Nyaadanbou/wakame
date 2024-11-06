package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.element.ElementProvider
import org.jetbrains.annotations.ApiStatus
import kotlin.random.Random


/**
 * 伤害捆绑包, 包含了一次伤害中的所有元素的[伤害包][DamagePacket].
 */
interface DamageBundle {
    /**
     * 该捆绑包的总伤害值.
     */
    fun total(): Double

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
    fun remove(element: Element): DamagePacket?

    /**
     * 从伤害捆绑包中移除元素伤害包.
     *
     * @param id 元素的标识
     */
    fun remove(id: String): DamagePacket?

    /**
     * 从伤害捆绑包中获取元素伤害包.
     */
    fun get(element: Element): DamagePacket?

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
    fun create(data: Map<Element, DamagePacket>): DamageBundle

    /**
     * 创建一个 [DamageBundle].
     * 传入的映射为 `element id` -> `damage packet`.
     * 如果 `element id` 不是有效的元素, 则会记录并忽略该元素.
     */
    fun createUnsafe(data: Map<String, DamagePacket>): DamageBundle

    /**
     * 伴生对象, 提供 [DamageBundleFactory] 的实例.
     */
    companion object Provider {
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
        element = ElementProvider.instance().get(elementId) ?: throw IllegalArgumentException("Invalid element: $elementId"),
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
    val element: Element,

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
    val rate: Double = .0,

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
     * 该伤害包的总伤害值, 称为“包伤害”.
     *
     * 伤害值在最大值与最小值之间的随机.
     * 每次调用都会返回一个新的随机结果.
     */
    fun damageValue(): Double {
        return if (min >= max) {
            max
        } else {
            Random.nextDouble(min, max)
        }
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

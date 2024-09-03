@file:OptIn(ExperimentalContracts::class)

package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.attribute.*
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registry.ElementRegistry
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * 开始构建一个 [DamageBundle].
 */
fun damageBundle(attrMap: AttributeMapLike, block: DamageBundleDSL.() -> Unit): DamageBundle {
    return DamageBundleDSL(attrMap).apply(block).get()
}

/**
 * 开始构建一个 [DamageBundle], 不依赖任何 [AttributeMapLike].
 */
fun damageBundle(block: DamageBundleDSL.() -> Unit): DamageBundle {
    return DamageBundleDSL().apply(block).get()
}

/**
 * 开始构建一个 [DamagePacket].
 */
fun damagePacket(element: Element, attrMap: AttributeMapLike, block: DamagePacketDSL.() -> Unit): DamagePacket {
    return DamagePacketDSL(element, attrMap).apply(block).build()
}

/**
 * 开始构建一个 [DamagePacket], 不依赖任何 [AttributeMapLike].
 */
fun damagePacket(element: Element, block: DamagePacketDSL.() -> Unit): DamagePacket {
    return DamagePacketDSL(element).apply(block).build()
}

/**
 * 开始构建一个 [DamagePacket], 使用默认的元素, 不依赖任何 [AttributeMapLike].
 */
fun damagePacket(block: DamagePacketDSL.() -> Unit): DamagePacket {
    return DamagePacketDSL(ElementRegistry.DEFAULT).apply(block).build()
}

/**
 * 用于标记 [DamageBundleDSL] 和 [DamagePacketDSL] 的 DSL.
 */
@DslMarker
annotation class DamagePacketBundleDsl

/**
 * [DamageBundle] 的 DSL.
 */
@DamagePacketBundleDsl
class DamageBundleDSL(
    private val attrMap: AttributeMapLike? = null,
) {
    private val bundle: DamageBundle = DamageBundle()

    private fun getElementById(id: String): Element? {
        return ElementRegistry.INSTANCES.find(id)
    }

    /**
     * 为每种已知的元素构建 [DamagePacket].
     */
    fun every(block: DamagePacketDSL.() -> Unit) {
        for ((_, element) in ElementRegistry.INSTANCES) {
            // every() 只添加先前不存在的元素伤害包, 使其永远成为一个 "fallback".
            // 这样无论 DSL 的调用顺序是怎样的, 都可以让 single() 拥有更高优先级.
            bundle.addIfAbsent(DamagePacketDSL(element, attrMap).apply(block).build())
        }
    }

    /**
     * 为默认的元素构建 [DamagePacket].
     */
    fun default(block: DamagePacketDSL.() -> Unit) {
        val element = ElementRegistry.DEFAULT
        bundle.add(DamagePacketDSL(element, attrMap).apply(block).build())
    }

    /**
     * 为指定的元素构建 [DamagePacket].
     */
    fun single(elementId: String, block: DamagePacketDSL.() -> Unit) {
        val element = getElementById(elementId) ?: run {
            DamageBundleDSLSupport.logger.warn("Element '$elementId' not found while building damage packet bundle. The damage packet will not be added.")
            return
        }
        bundle.add(DamagePacketDSL(element, attrMap).apply(block).build())
    }

    /**
     * 为指定的元素构建 [DamagePacket].
     */
    fun single(elementType: Element, block: DamagePacketDSL.() -> Unit) {
        bundle.add(DamagePacketDSL(elementType, attrMap).apply(block).build())
    }

    /**
     * 直接添加一个 [DamagePacket].
     */
    fun single(packet: DamagePacket) {
        bundle.add(packet)
    }

    /**
     * 返回构建好的 [DamageBundle].
     */
    fun get(): DamageBundle {
        return bundle
    }
}

/**
 * [DamagePacket] 的 DSL.
 */
@DamagePacketBundleDsl
class DamagePacketDSL(
    private val element: Element,
    private val attrMap: AttributeMapLike? = null,
) {
    private var min: Double? = null
    private var max: Double? = null
    private var rate: Double? = null
    private var defensePenetration: Double? = null
    private var defensePenetrationRate: Double? = null

    /**
     * 使用“标准”计算方式定义所有的值.
     */
    fun standard() {
        requireNotNull(attrMap) { "AttributeMapLike must not be null to use this DSL" }
        // 如果 ElementDamagePacket 的代码改动,
        // 并且有不止一个地方调用了 standard(),
        // 那么只需要改动这一个地方即可.
        min {
            standard()
        }
        max {
            standard()
        }
        rate {
            standard()
        }
        defensePenetration {
            standard()
        }
        defensePenetrationRate {
            standard()
        }
    }

    fun min(block: MinDamageDSL.() -> Double) {
        requireNotNull(attrMap) { "AttributeMapLike must not be null to use this DSL" }
        min = block(MinDamageDSL(element, attrMap))
    }

    fun min(value: Double) {
        min = value
    }

    fun max(block: MaxDamageDSL.() -> Double) {
        requireNotNull(attrMap) { "AttributeMapLike must not be null to use this DSL" }
        max = block(MaxDamageDSL(element, attrMap))
    }

    fun max(value: Double) {
        max = value
    }

    fun rate(block: DamageRateDSL.() -> Double) {
        requireNotNull(attrMap) { "AttributeMapLike must not be null to use this DSL" }
        rate = block(DamageRateDSL(element, attrMap))
    }

    fun rate(value: Double) {
        rate = value
    }

    fun defensePenetration(block: DefensePenetrationDSL.() -> Double) {
        requireNotNull(attrMap) { "AttributeMapLike must not be null to use this DSL" }
        defensePenetration = block(DefensePenetrationDSL(element, attrMap))
    }

    fun defensePenetration(value: Double) {
        defensePenetration = value
    }

    fun defensePenetrationRate(block: DefensePenetrationRateDSL.() -> Double) {
        requireNotNull(attrMap) { "AttributeMapLike must not be null to use this DSL" }
        defensePenetrationRate = block(DefensePenetrationRateDSL(element, attrMap))
    }

    fun defensePenetrationRate(value: Double) {
        defensePenetrationRate = value
    }

    fun build(): DamagePacket {
        return DamagePacket(
            element,
            validateValue(min),
            validateValue(max),
            validateValue(rate),
            validateValue(defensePenetration),
            validateValue(defensePenetrationRate)
        )
    }

    private fun <T> validateValue(value: T?): T {
        // 使用 contract 以告知编译器: 如果该函数成功返回, 那么 value 一定不为 null.
        contract {
            returns() implies (value != null)
        }
        return value ?: throw IllegalArgumentException(
            "A value is not present in the DSL object"
        )
    }

    @DamagePacketBundleDsl
    class MinDamageDSL(
        override val element: Element, override val attrMap: AttributeMapLike,
    ) : ValueDSL() {
        override fun standard(): Double {
            return (value { MIN_ATTACK_DAMAGE } + value(Attributes.UNIVERSAL_MIN_ATTACK_DAMAGE)).coerceAtLeast(0.0)
        }
    }

    @DamagePacketBundleDsl
    class MaxDamageDSL(
        override val element: Element, override val attrMap: AttributeMapLike,
    ) : ValueDSL() {
        override fun standard(): Double {
            return (value { MAX_ATTACK_DAMAGE } + value(Attributes.UNIVERSAL_MAX_ATTACK_DAMAGE)).coerceAtLeast(0.0)
        }
    }

    @DamagePacketBundleDsl
    class DamageRateDSL(
        override val element: Element, override val attrMap: AttributeMapLike,
    ) : ValueDSL() {
        override fun standard(): Double {
            return value { ATTACK_DAMAGE_RATE }
        }
    }

    @DamagePacketBundleDsl
    class DefensePenetrationDSL(
        override val element: Element, override val attrMap: AttributeMapLike,
    ) : ValueDSL() {
        override fun standard(): Double {
            return (value { DEFENSE_PENETRATION } + value(Attributes.UNIVERSAL_DEFENSE_PENETRATION)).coerceAtLeast(0.0)
        }
    }

    @DamagePacketBundleDsl
    class DefensePenetrationRateDSL(
        override val element: Element, override val attrMap: AttributeMapLike,
    ) : ValueDSL() {
        override fun standard(): Double {
            return (value { DEFENSE_PENETRATION_RATE } + value(Attributes.UNIVERSAL_DEFENSE_PENETRATION_RATE)).coerceAtLeast(0.0)
        }
    }

    abstract class ValueDSL {
        abstract val element: Element
        abstract val attrMap: AttributeMapLike

        /**
         * 使用“标准的”方式计算 [value].
         *
         * 实现类可以通过重写这个方法来定义自己的计算方式.
         */
        abstract fun standard(): Double

        // 开发日记 2024/7/22
        // 除了“标准”以外, 如果还有其他会大量用到的重复计算方式, 可以进一步扩展 DSL.

        /**
         * 获取指定的 [Attribute] 的值.
         */
        fun value(attribute: Attribute): Double {
            return attrMap.getValue(attribute)
        }

        /**
         * 获取指定的 [ElementAttribute] 的值.
         */
        fun value(block: ElementAttributes.() -> ElementAttribute): Double {
            val container = Attributes.element(element)
            val value = attrMap.getValue(block(container))
            return value
        }
    }
}

/**
 * 储存依赖注入进来的实例. 存到一个单例中, 以避免运行时的哈希开销.
 */
private object DamageBundleDSLSupport : KoinComponent {
    val logger: Logger by inject()
}
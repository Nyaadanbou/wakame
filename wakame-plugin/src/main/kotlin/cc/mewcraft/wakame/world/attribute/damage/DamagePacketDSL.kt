@file:OptIn(ExperimentalContracts::class)

package cc.mewcraft.wakame.world.attribute.damage

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeMap
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.attribute.ElementAttribute
import cc.mewcraft.wakame.attribute.ElementAttributeContainer
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
fun damageBundle(attrMap: AttributeMap, block: DamageBundleDSL.() -> Unit): DamageBundle {
    return DamageBundleDSL(attrMap).apply(block).get()
}

/**
 * 开始构建一个 [DamageBundle], 不依赖任何 [AttributeMap].
 */
fun damageBundle(block: DamageBundleDSL.() -> Unit): DamageBundle {
    return DamageBundleDSL().apply(block).get()
}

/**
 * 开始构建一个 [DamagePacket].
 */
fun damagePacket(element: Element, attrMap: AttributeMap, block: DamagePacketDSL.() -> Unit): DamagePacket {
    return DamagePacketDSL(element, attrMap).apply(block).build()
}

/**
 * 开始构建一个 [DamagePacket], 不依赖任何 [AttributeMap].
 */
fun damagePacket(element: Element, block: DamagePacketDSL.() -> Unit): DamagePacket {
    return DamagePacketDSL(element).apply(block).build()
}

/**
 * 开始构建一个 [DamagePacket], 使用默认的元素.
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
    private val attrMap: AttributeMap? = null,
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
            // 这样无论 DSL 的调用顺序是怎样的, 都可以让 element() 拥有更高优先级.
            bundle.addIfAbsent(DamagePacketDSL(element, attrMap).apply(block).build())
        }
    }

    /**
     * 为指定的元素构建 [DamagePacket].
     */
    fun element(id: String, block: DamagePacketDSL.() -> Unit) {
        val element = getElementById(id) ?: run {
            DamageBundleDSLSupport.logger.warn("Element '$id' not found while building damage packet bundle. The damage packet will not be added.")
            return
        }
        bundle.add(DamagePacketDSL(element, attrMap).apply(block).build())
    }

    /**
     * 为默认的元素构建 [DamagePacket].
     */
    fun default(block: DamagePacketDSL.() -> Unit) {
        val element = ElementRegistry.DEFAULT
        bundle.add(DamagePacketDSL(element, attrMap).apply(block).build())
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
    private val attrMap: AttributeMap? = null,
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
        requireNotNull(attrMap) { "AttributeMap must not be null to use this DSL" }
        // 如果 ElementDamagePacket 的代码改动,
        // 并且有不止一个地方调用了 standard(),
        // 那么只需要改动这一个地方即可.
        min { standard() }
        max { standard() }
        rate { standard() }
        defensePenetration { standard() }
        defensePenetrationRate { standard() }
    }

    fun min(block: MinDamageDSL.() -> Double) {
        requireNotNull(attrMap) { "AttributeMap must not be null to use this DSL" }
        min = block(MinDamageDSL(element, attrMap))
    }

    fun min(value: Double) {
        min = value
    }

    fun max(block: MaxDamageDSL.() -> Double) {
        requireNotNull(attrMap) { "AttributeMap must not be null to use this DSL" }
        max = block(MaxDamageDSL(element, attrMap))
    }

    fun max(value: Double) {
        max = value
    }

    fun rate(block: DamageRateDSL.() -> Double) {
        requireNotNull(attrMap) { "AttributeMap must not be null to use this DSL" }
        rate = block(DamageRateDSL(element, attrMap))
    }

    fun rate(value: Double) {
        rate = value
    }

    fun defensePenetration(block: DefensePenetrationDSL.() -> Double) {
        requireNotNull(attrMap) { "AttributeMap must not be null to use this DSL" }
        defensePenetration = block(DefensePenetrationDSL(element, attrMap))
    }

    fun defensePenetration(value: Double) {
        defensePenetration = value
    }

    fun defensePenetrationRate(block: DefensePenetrationRateDSL.() -> Double) {
        requireNotNull(attrMap) { "AttributeMap must not be null to use this DSL" }
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
        contract { returns() implies (value != null) }
        return value ?: throw IllegalArgumentException("A value is not present in the DSL object")
    }

    @DamagePacketBundleDsl
    class MinDamageDSL(
        override val element: Element, override val attrMap: AttributeMap,
    ) : ValueDSL() {
        override fun standard(): Double {
            return value { MIN_ATTACK_DAMAGE } + value(Attributes.UNIVERSAL_MIN_ATTACK_DAMAGE)
        }
    }

    @DamagePacketBundleDsl
    class MaxDamageDSL(
        override val element: Element, override val attrMap: AttributeMap,
    ) : ValueDSL() {
        override fun standard(): Double {
            return value { MAX_ATTACK_DAMAGE } + value(Attributes.UNIVERSAL_MAX_ATTACK_DAMAGE)
        }
    }

    @DamagePacketBundleDsl
    class DamageRateDSL(
        override val element: Element, override val attrMap: AttributeMap,
    ) : ValueDSL() {
        override fun standard(): Double {
            return value { ATTACK_DAMAGE_RATE } + value(Attributes.UNIVERSAL_ATTACK_DAMAGE_RATE)
        }
    }

    @DamagePacketBundleDsl
    class DefensePenetrationDSL(
        override val element: Element, override val attrMap: AttributeMap,
    ) : ValueDSL() {
        override fun standard(): Double {
            return value { DEFENSE_PENETRATION } + value(Attributes.UNIVERSAL_DEFENSE_PENETRATION)
        }
    }

    @DamagePacketBundleDsl
    class DefensePenetrationRateDSL(
        override val element: Element, override val attrMap: AttributeMap,
    ) : ValueDSL() {
        override fun standard(): Double {
            return value { DEFENSE_PENETRATION_RATE } + value(Attributes.UNIVERSAL_DEFENSE_PENETRATION_RATE)
        }
    }

    abstract class ValueDSL {
        abstract val element: Element
        abstract val attrMap: AttributeMap

        /**
         * 使用“标准的”计算方式计算 [value].
         */
        abstract fun standard(): Double

        /**
         * 获取指定的 [Attribute] 的值.
         */
        fun value(attribute: Attribute): Double {
            return attrMap.getValue(attribute)
        }

        /**
         * 获取指定的 [ElementAttribute] 的值.
         */
        fun value(block: ElementAttributeContainer.() -> ElementAttribute): Double {
            val container = Attributes.byElement(element)
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
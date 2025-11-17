@file:JvmName("BundleDsl")

package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.entity.attribute.*
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.entry.RegistryEntry

/**
 * 开始构建一个 [DamageBundle].
 */
fun damageBundle(attrMap: AttributeMapLike, block: DamageBundleDsl.() -> Unit): DamageBundle {
    return DamageBundleDsl(attrMap).apply(block).get()
}

/**
 * 开始构建一个 [DamageBundle], 不依赖任何 [AttributeMapLike].
 */
fun damageBundle(block: DamageBundleDsl.() -> Unit): DamageBundle {
    return DamageBundleDsl().apply(block).get()
}

/**
 * 开始构建一个 [DamagePacket].
 */
fun damagePacket(element: RegistryEntry<Element>, attrMap: AttributeMapLike, block: DamagePacketDsl.() -> Unit): DamagePacket {
    return DamagePacketDsl(element, attrMap).apply(block).build()
}

/**
 * 开始构建一个 [DamagePacket], 不依赖任何 [AttributeMapLike].
 */
fun damagePacket(element: RegistryEntry<Element>, block: DamagePacketDsl.() -> Unit): DamagePacket {
    return DamagePacketDsl(element).apply(block).build()
}

/**
 * 开始构建一个 [DamagePacket], 使用默认的元素, 不依赖任何 [AttributeMapLike].
 */
fun damagePacket(block: DamagePacketDsl.() -> Unit): DamagePacket {
    return DamagePacketDsl(BuiltInRegistries.ELEMENT.getDefaultEntry()).apply(block).build()
}

/**
 * 用于标记 [DamageBundleDsl] 和 [DamagePacketDsl] 的 DSL.
 */
@DslMarker
annotation class DamagePacketBundleDsl

/**
 * [DamageBundle] 的 DSL.
 */
@DamagePacketBundleDsl
class DamageBundleDsl(
    private val attrMap: AttributeMapLike? = null,
) {
    private val bundle: DamageBundle.Builder = DamageBundle.builder()

    private fun getElementById(id: String): RegistryEntry<Element>? {
        return BuiltInRegistries.ELEMENT.getEntry(id)
    }

    /**
     * 为每种已知的元素构建 [DamagePacket].
     */
    fun every(block: DamagePacketDsl.() -> Unit) {
        for (element in BuiltInRegistries.ELEMENT.entrySequence) {
            // every() 只添加先前不存在的元素伤害包, 使其永远成为一个 "fallback".
            // 这样无论 DSL 的调用顺序是怎样的, 都可以让 single() 拥有更高优先级.
            bundle.addIfAbsent(DamagePacketDsl(element, attrMap).apply(block).build())
        }
    }

    /**
     * 为默认的元素构建 [DamagePacket].
     */
    fun default(block: DamagePacketDsl.() -> Unit) {
        val element = BuiltInRegistries.ELEMENT.getDefaultEntry()
        bundle.add(DamagePacketDsl(element, attrMap).apply(block).build())
    }

    /**
     * 为指定的元素构建 [DamagePacket].
     */
    fun single(elementId: String, block: DamagePacketDsl.() -> Unit) {
        val element = getElementById(elementId)
        if (element == null) {
            LOGGER.warn("Element '$elementId' not found while building damage packet bundle. The damage packet will not be added.")
            return
        }
        bundle.add(DamagePacketDsl(element, attrMap).apply(block).build())
    }

    /**
     * 为指定的元素构建 [DamagePacket].
     */
    fun single(element: RegistryEntry<Element>, block: DamagePacketDsl.() -> Unit) {
        bundle.add(DamagePacketDsl(element, attrMap).apply(block).build())
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
        return bundle.build()
    }
}

/**
 * [DamagePacket] 的 DSL.
 */
@DamagePacketBundleDsl
class DamagePacketDsl(
    private val element: RegistryEntry<Element>,
    private val attrMap: AttributeMapLike? = null,
) {
    private var min: Double = Double.NaN
    private var max: Double = Double.NaN
    private var rate: Double = Double.NaN
    private var defensePenetration: Double = Double.NaN
    private var defensePenetrationRate: Double = Double.NaN

    /**
     * 使用“标准”计算方式定义所有的值.
     */
    fun standard() {
        requireNotNull(attrMap) { "AttributeMapLike must not be null to use this DSL" }
        // 如果 ElementDamagePacket 的代码改动,
        // 并且有不止一个地方调用了 standard(),
        // 那么只需要改动这一个地方即可.
        min(MinDamageDsl::standard)
        max(MaxDamageDsl::standard)
        rate(DamageRateDsl::standard)
        defensePenetration(DefensePenetrationDsl::standard)
        defensePenetrationRate(DefensePenetrationRateDsl::standard)
    }

    fun min(block: MinDamageDsl.() -> Double) {
        requireNotNull(attrMap) { "AttributeMapLike must not be null to use this DSL" }
        min = block(MinDamageDsl(element, attrMap))
    }

    fun min(value: Double) {
        min = value
    }

    fun max(block: MaxDamageDsl.() -> Double) {
        requireNotNull(attrMap) { "AttributeMapLike must not be null to use this DSL" }
        max = block(MaxDamageDsl(element, attrMap))
    }

    fun max(value: Double) {
        max = value
    }

    fun rate(block: DamageRateDsl.() -> Double) {
        requireNotNull(attrMap) { "AttributeMapLike must not be null to use this DSL" }
        rate = block(DamageRateDsl(element, attrMap))
    }

    fun rate(value: Double) {
        rate = value
    }

    fun defensePenetration(block: DefensePenetrationDsl.() -> Double) {
        requireNotNull(attrMap) { "AttributeMapLike must not be null to use this DSL" }
        defensePenetration = block(DefensePenetrationDsl(element, attrMap))
    }

    fun defensePenetration(value: Double) {
        defensePenetration = value
    }

    fun defensePenetrationRate(block: DefensePenetrationRateDsl.() -> Double) {
        requireNotNull(attrMap) { "AttributeMapLike must not be null to use this DSL" }
        defensePenetrationRate = block(DefensePenetrationRateDsl(element, attrMap))
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

    private fun validateValue(value: Double): Double {
        require(!value.isNaN()) { "A value is not present in the DSL object" }
        return value
    }

    @DamagePacketBundleDsl
    class MinDamageDsl(
        override val element: RegistryEntry<Element>, override val attrMap: AttributeMapLike,
    ) : ValueDsl() {
        override fun standard(): Double {
            return (value(Attributes.MIN_ATTACK_DAMAGE) + value(Attributes.UNIVERSAL_MIN_ATTACK_DAMAGE)).coerceAtLeast(0.0)
        }
    }

    @DamagePacketBundleDsl
    class MaxDamageDsl(
        override val element: RegistryEntry<Element>, override val attrMap: AttributeMapLike,
    ) : ValueDsl() {
        override fun standard(): Double {
            return (value(Attributes.MAX_ATTACK_DAMAGE) + value(Attributes.UNIVERSAL_MAX_ATTACK_DAMAGE)).coerceAtLeast(0.0)
        }
    }

    @DamagePacketBundleDsl
    class DamageRateDsl(
        override val element: RegistryEntry<Element>, override val attrMap: AttributeMapLike,
    ) : ValueDsl() {
        override fun standard(): Double {
            return value(Attributes.ATTACK_DAMAGE_RATE)
        }
    }

    @DamagePacketBundleDsl
    class DefensePenetrationDsl(
        override val element: RegistryEntry<Element>, override val attrMap: AttributeMapLike,
    ) : ValueDsl() {
        override fun standard(): Double {
            return (value(Attributes.DEFENSE_PENETRATION) + value(Attributes.UNIVERSAL_DEFENSE_PENETRATION)).coerceAtLeast(0.0)
        }
    }

    @DamagePacketBundleDsl
    class DefensePenetrationRateDsl(
        override val element: RegistryEntry<Element>, override val attrMap: AttributeMapLike,
    ) : ValueDsl() {
        override fun standard(): Double {
            return (value(Attributes.DEFENSE_PENETRATION_RATE) + value(Attributes.UNIVERSAL_DEFENSE_PENETRATION_RATE)).coerceAtLeast(0.0)
        }
    }

    abstract class ValueDsl {
        abstract val element: RegistryEntry<Element>
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
         * 获取指定的 [cc.mewcraft.wakame.entity.attribute.Attribute] 的值.
         */
        fun value(attribute: Attribute): Double {
            return attrMap.getValue(attribute)
        }

        /**
         * 获取指定的 [ElementAttribute] 的值.
         */
        fun value(attributeGetter: AttributeGetter): Double {
            return attrMap.getValue(attributeGetter.of(element))
        }
    }
}

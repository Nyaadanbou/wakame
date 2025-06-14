@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.entity.attribute.AttributeMap
import cc.mewcraft.wakame.entity.attribute.AttributeMapLike
import cc.mewcraft.wakame.entity.attribute.Attributes
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import it.unimi.dsi.fastutil.objects.Reference2DoubleMap
import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap
import kotlin.math.absoluteValue
import kotlin.random.Random

/**
 * 伤害元数据, 包含了一次伤害中"攻击阶段"的有关信息.
 * 这些信息均由伤害发起者计算而来.
 * 一旦实例化后, 各种信息就已经确定并不允许修改.
 */
data class DamageMetadata(
    /**
     * 伤害标签.
     */
    @Deprecated("待移除")
    val damageTags: DamageTags,

    /**
     * 攻击者伤害捆绑包.
     */
    val damageBundle: DamageBundle,

    /**
     * 攻击者暴击元数据.
     */
    val criticalStrikeMetadata: CriticalStrikeMetadata,

    /**
     * 此攻击是否忽略无懈可击期间的伤害减免.
     */
    val ignoreInvulnerability: Boolean = false,

    /**
     * 此攻击是否忽略格挡的伤害减免.
     */
    val ignoreBlocking: Boolean = false,

    /**
     * 此攻击是否忽略抗性提升药水效果的伤害减免.
     */
    val ignoreResistance: Boolean = false,

    /**
     * 此攻击是否忽略伤害吸收.
     * 若忽略则跳过黄心直接扣除红心.
     * 实际上红心+黄心的总损失量不变.
     */
    val ignoreAbsorption: Boolean = false,
)

/**
 * 伤害的暴击元数据.
 */
data class CriticalStrikeMetadata(
    /**
     * 暴击倍率的值.
     */
    val power: Double,

    /**
     * 这次伤害的暴击状态.
     */
    val state: CriticalStrikeState,
) {
    companion object Constants {
        /**
         * 默认的暴击元数据.
         * 用于不会暴击的攻击.
         */
        @JvmField
        val NONE: CriticalStrikeMetadata = CriticalStrikeMetadata(1.0, CriticalStrikeState.NONE)
    }
}

/**
 * 暴击状态.
 */
enum class CriticalStrikeState {
    /**
     * 正暴击.
     */
    POSITIVE,

    /**
     * 负暴击.
     */
    NEGATIVE,

    /**
     * 无暴击.
     */
    NONE
}

//<editor-fold desc="CriticalStrikeMetadata">
/**
 * 通过属性计算和随机过程创建一个 [CriticalStrikeMetadata].
 */
fun CriticalStrikeMetadata(chance: Double, positivePower: Double, negativePower: Double, nonePower: Double): CriticalStrikeMetadata {
    val power: Double
    val state: CriticalStrikeState
    if (chance < 0) {
        if (Random.nextDouble() < chance.absoluteValue) {
            state = CriticalStrikeState.NEGATIVE
            power = negativePower
        } else {
            state = CriticalStrikeState.NONE
            power = nonePower
        }
    } else {
        if (Random.nextDouble() < chance) {
            state = CriticalStrikeState.POSITIVE
            power = positivePower
        } else {
            state = CriticalStrikeState.NONE
            power = nonePower
        }
    }
    return CriticalStrikeMetadata(power, state)
}

fun CriticalStrikeMetadata(attributeMap: AttributeMap): CriticalStrikeMetadata {
    return CriticalStrikeMetadata(
        attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE),
        attributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER),
        attributeMap.getValue(Attributes.NEGATIVE_CRITICAL_STRIKE_POWER),
        attributeMap.getValue(Attributes.NONE_CRITICAL_STRIKE_POWER)
    )
}
//</editor-fold>

//<editor-fold desc="DamageMetadata Constructors">
/**
 * 非生物造成的伤害元数据.
 * 用于给特定原版伤害类型加上元素和护甲穿透.
 * 如: 给溺水伤害加上水元素, 给着火伤害加上火元素.
 * 或用于无来源的弹射物的伤害.
 */
object VanillaDamageMetadata {
    operator fun invoke(damageBundle: DamageBundle): DamageMetadata {
        return DamageMetadata(
            damageTags = DamageTags(),
            damageBundle = damageBundle,
            criticalStrikeMetadata = CriticalStrikeMetadata.NONE
        )
    }

    operator fun invoke(element: RegistryEntry<Element>, damageValue: Double, defensePenetration: Double, defensePenetrationRate: Double): DamageMetadata {
        return invoke(
            damageBundle {
                single(element) {
                    min(damageValue)
                    max(damageValue)
                    rate(1.0)
                    defensePenetration(defensePenetration)
                    defensePenetrationRate(defensePenetrationRate)
                }
            }
        )
    }

    operator fun invoke(damageValue: Double): DamageMetadata {
        return invoke(BuiltInRegistries.ELEMENT.getDefaultEntry(), damageValue, 0.0, 0.0)
    }
}

object PlayerDamageMetadata {
    /**
     * 返回一个`1`点默认元素伤害的 [DamageMetadata].
     *
     * 使用场景:
     * - 使用 *空手* 造成的伤害.
     * - 使用 *没有武器行为的物品* 造成的伤害.
     */
    @JvmField
    val INTRINSIC_ATTACK: DamageMetadata = DamageMetadata(
        damageTags = DamageTags(DamageTag.HAND),
        damageBundle = damageBundle {
            default {
                min(1.0)
                max(1.0)
                rate(1.0)
                defensePenetration(.0)
                defensePenetrationRate(.0)
            }
        },
        criticalStrikeMetadata = CriticalStrikeMetadata.NONE
    )

    operator fun invoke(attributes: AttributeMapLike, damageBundle: DamageBundle, damageTags: DamageTags = DamageTags()): DamageMetadata {
        return DamageMetadata(
            damageTags = damageTags,
            damageBundle = damageBundle,
            criticalStrikeMetadata = CriticalStrikeMetadata(
                chance = attributes.getValue(Attributes.CRITICAL_STRIKE_CHANCE),
                positivePower = attributes.getValue(Attributes.CRITICAL_STRIKE_POWER),
                negativePower = attributes.getValue(Attributes.NEGATIVE_CRITICAL_STRIKE_POWER),
                nonePower = attributes.getValue(Attributes.NONE_CRITICAL_STRIKE_POWER)
            )
        )
    }

    operator fun invoke(attributes: AttributeMapLike, builder: DamageBundleDsl.() -> Unit): DamageMetadata {
        return invoke(
            attributes = attributes,
            damageBundle = damageBundle(attributes, builder),
            damageTags = DamageTags()
        )
    }
}

object EntityDamageMetadata {
    operator fun invoke(damageBundle: DamageBundle, criticalStrikeMetadata: CriticalStrikeMetadata, damageTags: DamageTags): DamageMetadata {
        return DamageMetadata(
            damageTags = damageTags,
            damageBundle = damageBundle,
            criticalStrikeMetadata = criticalStrikeMetadata
        )
    }

    operator fun invoke(damageBundle: DamageBundle, criticalStrikeMetadata: CriticalStrikeMetadata): DamageMetadata {
        return invoke(damageBundle, criticalStrikeMetadata, DamageTags())
    }
}
//</editor-fold>

/**
 * 防御元数据, 包含了一次伤害中"防御阶段"的有关信息.
 * 这些信息均由伤害承受者计算而来.
 * 一旦实例化后, 各种信息就已经确定并不允许修改.
 */
data class DefenseMetadata(
    /**
     * 受伤者各元素防御值.
     */
    val defenseMap: Reference2DoubleMap<RegistryEntry<Element>>,

    /**
     * 受伤者各元素承伤倍率值.
     */
    val incomingDamageRateMap: Reference2DoubleMap<RegistryEntry<Element>>,

    /**
     * 受伤者格挡减伤.
     */
    val isBlocking: Boolean,

    /**
     * 受伤者抗性提升状态效果等级.
     */
    val resistanceLevel: Int,
) {
    constructor(
        damageeAttributes: AttributeMap,
        isBlocking: Boolean,
        resistanceLevel: Int
    ) : this(
        defenseMap = damageeAttributes.getDefenseMap(),
        incomingDamageRateMap = damageeAttributes.getIncomingDamageRateMap(),
        isBlocking = isBlocking,
        resistanceLevel = resistanceLevel
    )

    fun getElementDefense(elementType:RegistryEntry<Element>): Double{
        // 默认值是0.0
        return defenseMap.getDouble(elementType)
    }

    fun getElementIncomingDamageRate(elementType:RegistryEntry<Element>): Double{
        // 默认值是1.0
        return incomingDamageRateMap.getDouble(elementType)
    }
}

private fun AttributeMap.getDefenseMap(): Reference2DoubleMap<RegistryEntry<Element>> {
    val map = Reference2DoubleOpenHashMap<RegistryEntry<Element>>()
    for (elementType in BuiltInRegistries.ELEMENT.entrySequence) {
        // 防御为对应元素防御 + 通用防御
        // 强制不小于0
        val defenseValue = getValue(Attributes.DEFENSE.of(elementType)) + getValue(Attributes.UNIVERSAL_DEFENSE)
        map[elementType] = defenseValue.coerceAtLeast(0.0)
    }
    return map
}

private fun AttributeMap.getIncomingDamageRateMap(): Reference2DoubleMap<RegistryEntry<Element>> {
    val map = Reference2DoubleOpenHashMap<RegistryEntry<Element>>()
    // 承伤倍率默认值为1.0
    map.defaultReturnValue(1.0)
    for (elementType in BuiltInRegistries.ELEMENT.entrySequence) {
        val incomingDamageRate = getValue(Attributes.INCOMING_DAMAGE_RATE.of(elementType))
        map[elementType] = incomingDamageRate
    }
    return map
}
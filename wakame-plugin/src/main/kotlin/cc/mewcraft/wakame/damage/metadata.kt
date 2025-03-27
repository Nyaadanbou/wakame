@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.attribute.AttributeMap
import cc.mewcraft.wakame.attribute.AttributeMapSnapshot
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.element.ElementType
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import kotlin.math.absoluteValue
import kotlin.random.Random

/**
 * 伤害元数据, 包含了一次伤害中"攻击阶段"的有关信息.
 * 一旦实例化后, 攻击伤害的数值以及各种信息就已经确定.
 */
data class DamageMetadata(
    /**
     * 伤害标签.
     */
    val damageTags: DamageTags,

    /**
     * 伤害捆绑包.
     */
    val damageBundle: DamageBundle,

    /**
     * 暴击元数据.
     */
    val criticalStrikeMetadata: CriticalStrikeMetadata,
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

// ------------
// 内部实现
// ------------

//<editor-fold desc="CriticalStrikeMetadata">
/**
 * 通过属性计算和随机过程创建一个 [CriticalStrikeMetadata].
 */
internal fun CriticalStrikeMetadata(chance: Double, positivePower: Double, negativePower: Double, nonePower: Double): CriticalStrikeMetadata {
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

internal fun CriticalStrikeMetadata(attributeMap: AttributeMap): CriticalStrikeMetadata {
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
internal object VanillaDamageMetadata {
    operator fun invoke(damageBundle: DamageBundle): DamageMetadata {
        return DamageMetadata(
            damageTags = DamageTags(),
            damageBundle = damageBundle,
            criticalStrikeMetadata = CriticalStrikeMetadata.NONE
        )
    }

    operator fun invoke(element: RegistryEntry<ElementType>, damageValue: Double, defensePenetration: Double, defensePenetrationRate: Double): DamageMetadata {
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
        return invoke(KoishRegistries.ELEMENT.getDefaultEntry(), damageValue, 0.0, 0.0)
    }
}

internal object PlayerDamageMetadata {
    /**
     * 返回一个`1`点默认元素伤害的 [DamageMetadata].
     *
     * 使用场景:
     * - 使用 *空手* 造成的伤害.
     * - 使用 *没有攻击行为的物品* 造成的伤害.
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

    operator fun invoke(attributes: AttributeMapSnapshot, damageBundle: DamageBundle, damageTags: DamageTags): DamageMetadata {
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
}

internal object EntityDamageMetadata {
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

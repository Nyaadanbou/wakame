package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.attribute.AttributeMap
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.element.ElementType
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.round

/**
 * 防御元数据.
 * 包含了一次伤害中“防御阶段”的有关信息.
 * 在实例化后, 最终伤害数值以及各种信息就已经确定了.
 */
sealed interface DefenseMetadata {
    val damageeAttributeMap: AttributeMap

    /**
     * 计算各元素最终伤害的方法.
     */
    fun calculateFinalDamage(element: RegistryEntry<ElementType>, damageMetadata: DamageMetadata): Double
}

/**
 * 玩家和非玩家实体的防御元数据.
 */
class EntityDefenseMetadata(
    override val damageeAttributeMap: AttributeMap,
) : DefenseMetadata {
    override fun calculateFinalDamage(element: RegistryEntry<ElementType>, damageMetadata: DamageMetadata): Double {
        // 当该元素的伤害包不存在时, 返回 0.0
        val packet = damageMetadata.damageBundle.get(element) ?: return 0.0

        // 该元素伤害倍率(或称攻击威力)
        val attackDamageRate = packet.rate
        // 暴击倍率
        val criticalStrikePower = damageMetadata.criticalStrikeMetadata.power
        // 受伤者防御, 不会小于0
        val defense = (damageeAttributeMap.getValue(Attributes.DEFENSE.of(element)) + damageeAttributeMap.getValue(Attributes.UNIVERSAL_DEFENSE)).coerceAtLeast(0.0)
        // 受伤者承伤倍率
        val incomingDamageRate = damageeAttributeMap.getValue(Attributes.INCOMING_DAMAGE_RATE.of(element))

        // 计算原始伤害
        var originalDamage = packet.packetDamage
        if (DamageRules.ATTACK_DAMAGE_RATE_MULTIPLY_BEFORE_DEFENSE) {
            originalDamage *= attackDamageRate
        }
        if (DamageRules.CRITICAL_STRIKE_POWER_MULTIPLY_BEFORE_DEFENSE) {
            originalDamage *= criticalStrikePower
        }

        // 计算有效防御
        val validDefense = DamageRules.calculateValidDefense(
            defense = defense,
            defensePenetration = packet.defensePenetration,
            defensePenetrationRate = packet.defensePenetrationRate
        )

        // 计算防御后伤害
        val damageAfterDefense = DamageRules.calculateDamageAfterDefense(
            originalDamage = originalDamage,
            validDefense = validDefense
        )

        // 计算最终伤害
        var finalDamage = damageAfterDefense * incomingDamageRate
        if (!DamageRules.ATTACK_DAMAGE_RATE_MULTIPLY_BEFORE_DEFENSE) {
            finalDamage *= attackDamageRate
        }
        if (!DamageRules.CRITICAL_STRIKE_POWER_MULTIPLY_BEFORE_DEFENSE) {
            finalDamage *= criticalStrikePower
        }
        val leastDamage = if (packet.packetDamage > 0) DamageRules.LEAST_DAMAGE else 0.0
        finalDamage = finalDamage.coerceAtLeast(leastDamage)

        if (DamageRules.ROUNDING_DAMAGE){
            finalDamage = round(finalDamage)
        }

        return finalDamage
    }
}

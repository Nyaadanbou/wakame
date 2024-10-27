package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.attribute.AttributeMap
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.element.Element

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
    fun calculateFinalDamage(element: Element, damageMetadata: DamageMetadata): Double
}

/**
 * 玩家和非玩家实体的防御元数据.
 */
class EntityDefenseMetadata(
    override val damageeAttributeMap: AttributeMap,
) : DefenseMetadata {
    override fun calculateFinalDamage(element: Element, damageMetadata: DamageMetadata): Double {
        // 当该元素的伤害包不存在时, 返回 0.0
        val packet = damageMetadata.damageBundle.get(element) ?: return 0.0

        // 计算防御后伤害
        val damageAfterDefense = DamageRules.calculateDamageAfterDefense(
            packet.packetDamage,
            (damageeAttributeMap.getValue(Attributes.element(element).DEFENSE) + damageeAttributeMap.getValue(Attributes.UNIVERSAL_DEFENSE)).coerceAtLeast(0.0),
            packet.defensePenetration,
            packet.defensePenetrationRate
        )

        // 依次考虑防御力、元素伤害倍率、百分比伤害修正、暴击倍率
        val criticalPower = if (damageMetadata.criticalStrikeMetadata.state == CriticalStrikeState.NONE) 1.0 else damageMetadata.criticalStrikeMetadata.power
        val incomingDamageRate = damageeAttributeMap.getValue(Attributes.element(element).INCOMING_DAMAGE_RATE)

        return (damageAfterDefense * packet.rate * incomingDamageRate * criticalPower).coerceAtLeast(if (packet.packetDamage > 0) 1.0 else 0.0)
    }
}

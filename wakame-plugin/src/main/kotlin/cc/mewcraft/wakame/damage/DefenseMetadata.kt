package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.attribute.AttributeMap
import cc.mewcraft.wakame.attribute.Attributes

/**
 * 防御元数据.
 * 包含了一次伤害中“防御阶段”的有关信息.
 * 在实例化后, 最终伤害数值以及各种信息就已经确定了.
 */
sealed interface DefenseMetadata {
    val damageeAttributeMap: AttributeMap
    fun calculateFinalDamage(damageMetadata: DamageMetadata): Double
}

/**
 * 玩家和非玩家实体的防御元数据.
 */
class EntityDefenseMetadata(
    override val damageeAttributeMap: AttributeMap,
) : DefenseMetadata {
    override fun calculateFinalDamage(damageMetadata: DamageMetadata): Double {
        var totalElementDamage = 0.0
        val criticalPower = if (damageMetadata.isCritical) damageMetadata.criticalPower else 1.0
        for (packet in damageMetadata.damageBundle.packets()) {
            val element = packet.element
            val damageAfterDefense = DamageRules.calculateDamageAfterDefense(
                packet.packetDamage,
                damageeAttributeMap.getValue(Attributes.byElement(element).DEFENSE),
                packet.defensePenetration,
                packet.defensePenetrationRate
            )
            val incomingDamageRate = damageeAttributeMap.getValue(Attributes.UNIVERSAL_INCOMING_DAMAGE_RATE) +
                    damageeAttributeMap.getValue(Attributes.byElement(element).INCOMING_DAMAGE_RATE)
            // 依次计算防御力、元素伤害百分比加成、考虑承伤百分比、最小伤害、暴击倍率
            totalElementDamage += (damageAfterDefense *
                    (1 + packet.rate) *
                    (1 + incomingDamageRate)).coerceAtLeast(if (packet.packetDamage > 0) 1.0 else 0.0) *
                    criticalPower
        }
        return totalElementDamage
    }
}

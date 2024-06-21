package cc.mewcraft.wakame.world.attribute.damage

import cc.mewcraft.wakame.attribute.AttributeMap
import cc.mewcraft.wakame.attribute.Attributes

/**
 * 防御元数据
 * 包含了一次伤害中“防御阶段”的有关信息
 * 其实现类实例化时，最终伤害值以及各种信息就已经确定了
 */
sealed interface DefenseMetaData {
    val damageeAttributeMap: AttributeMap
    fun calculateFinalDamage(damageMetaData: DamageMetaData): Double
}

/**
 * 玩家和非玩家实体的防御元数据
 */
class EntityDefenseMetaData(
    override val damageeAttributeMap: AttributeMap,
) : DefenseMetaData {
    override fun calculateFinalDamage(damageMetaData: DamageMetaData): Double {
        var totalElementDamage = 0.0
        for (packet in damageMetaData.packets) {
            val element = packet.element
            //考虑防御后伤害、元素伤害减免（本元素+通用元素）
            val damageAfterDefense = DamageRules.calculateDamageAfterDefense(
                packet.packetDamage,
                damageeAttributeMap.getValue(Attributes.byElement(element).DEFENSE),
                packet.defensePenetration
            )

            val incomingDamageRate = (damageeAttributeMap.getValue(Attributes.UNIVERSAL_INCOMING_DAMAGE_RATE)
                    + damageeAttributeMap.getValue(Attributes.byElement(element).INCOMING_DAMAGE_RATE))

            totalElementDamage += damageAfterDefense * (1 + incomingDamageRate)
        }
        //防御后，再考虑暴击
        val criticalPower = if (damageMetaData.isCritical) damageMetaData.criticalPower else 1.0
        return totalElementDamage * criticalPower
    }
}

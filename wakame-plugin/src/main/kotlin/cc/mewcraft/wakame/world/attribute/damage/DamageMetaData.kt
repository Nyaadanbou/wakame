package cc.mewcraft.wakame.world.attribute.damage

import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.user.User
import me.lucko.helper.random.VariableAmount
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import kotlin.random.Random

/**
 * 伤害元数据
 * 包含了一次伤害中“攻击阶段”的有关信息
 */
sealed interface DamageMetaData {
    val damageValue: Double
}

/**
 * 原版伤害的元数据
 * 如：细雪冻伤伤害、岩浆块烫脚伤害
 * 理论上不做任何处理，只包含伤害值这一信息
 */
class VanillaDamageMetaData(
    override val damageValue: Double
) : DamageMetaData

/**
 * 玩家近战攻击造成的伤害元数据
 * 如：玩家使用剑攻击生物
 */
class PlayerMeleeAttackMetaData(
    val user: User<Player>,
) : DamageMetaData {
    private val packets: List<ElementDamagePacket> = generatePackets()
    override val damageValue: Double = calculateDamageValue()

    /**
     * 在元素伤害包生成时，所有的随机就已经确定了，包括：
     * 伤害值 value 在范围 [max,min] 的随机
     * 该元素伤害是否暴击的随机
     */
    private fun generatePackets(): List<ElementDamagePacket> {
        val attributeMap = user.attributeMap
        val list = arrayListOf<ElementDamagePacket>()
        for (it in ElementRegistry.INSTANCES.objects) {
            list.add(
                ElementDamagePacket(
                    it,
                    attributeMap.getValue(Attributes.byElement(it).MAX_ATTACK_DAMAGE),
                    attributeMap.getValue(Attributes.byElement(it).MIN_ATTACK_DAMAGE),
                    attributeMap.getValue(Attributes.byElement(it).ATTACK_DAMAGE_RATE),
                    attributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER),
                    Random.nextDouble() < attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE)
                )
            )
        }
        return list
    }

    /**
     * 玩家近战伤害的计算
     * 最终伤害 = Σ random(MIN_ATTACK_DAMAGE, MAX_ATTACK_DAMAGE) * (1 + ATTACK_DAMAGE_RATE) * (1 + CRITICAL_STRIKE_POWER)
     */
    private fun calculateDamageValue(): Double {
        var finalDamage = 0.0
        packets.forEach {
            finalDamage += it.value * (1 + it.rate) * (1.0 + if (it.isCritical) it.criticalPower else 0.0)
        }
        return finalDamage
    }
}

/**
 * 非玩家实体近战攻击造成的伤害元数据
 * 如：僵尸近战攻击、河豚接触伤害
 */
class EntityMeleeAttackMetaData(
    entity: LivingEntity
) : DamageMetaData {
    override val damageValue: Double = TODO("实体伤害的获取和计算")
}

/**
 * 玩家使用弹射物造成伤害的元数据
 * 如：玩家射出的箭矢、三叉戟、雪球击中实体
 */
class PlayerProjectileMetaData(
    val user: User<Player>,
) : DamageMetaData {
    override val damageValue: Double = TODO("玩家弹射物攻击的获取和计算")
}

/**
 * 非玩家实体使用弹射物造成伤害的元数据
 * 如：骷髅射出的箭矢、溺尸射出的三叉戟、监守者的音爆击中实体
 */
class EntityProjectileMetaData(
    entity: LivingEntity
) : DamageMetaData {
    override val damageValue: Double = TODO("实体弹射物攻击的获取和计算")
}

data class ElementDamagePacket(
    val element: Element,
    val min: Double,
    val max: Double,
    val rate: Double,
    val criticalPower: Double,
    val isCritical: Boolean
) {
    val value: Double = VariableAmount.range(max, min).amount
}
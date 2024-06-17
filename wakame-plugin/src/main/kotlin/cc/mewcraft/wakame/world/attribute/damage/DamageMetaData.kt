package cc.mewcraft.wakame.world.attribute.damage

import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.binary.PlayNekoStack
import cc.mewcraft.wakame.item.hasBehavior
import cc.mewcraft.wakame.item.schema.behavior.Arrow
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
    val packets: List<ElementDamagePacket>
    val damageValue: Double
}

/**
 * 原版伤害的元数据
 * 如：细雪冻伤伤害、岩浆块烫脚伤害
 * 理论上不做任何处理，只包含伤害值这一信息
 */
class VanillaDamageMetaData(
    override val damageValue: Double
) : DamageMetaData {
    override val packets: List<ElementDamagePacket> = listOf(generateVanillaPacket())

    private fun generateVanillaPacket(): ElementDamagePacket {
        return ElementDamagePacket(
            ElementRegistry.DEFAULT,
            damageValue,
            damageValue,
            0.0,
            0.0,
            false,
            0.0
        )
    }
}

/**
 * 玩家近战攻击造成的伤害元数据
 * 如：玩家使用剑攻击生物
 * [isSweep] 表示这次伤害是否是横扫造成的
 * 会根据横扫的逻辑削弱伤害
 */
class PlayerMeleeAttackMetaData(
    val user: User<Player>,
    val isSweep: Boolean
) : DamageMetaData {
    override val packets: List<ElementDamagePacket> = generatePackets(isSweep)
    override val damageValue: Double = packets.sumOf { it.finalDamage }

    private fun generatePackets(isSweep: Boolean): List<ElementDamagePacket> {
        if (isSweep) return generateSweepPackets()
        return generateNotSweepPackets()
    }


    /**
     * 在元素伤害包生成时，所有的随机就已经确定了，包括：
     * 伤害值 value 在范围 [max,min] 的随机
     * 该元素伤害是否暴击的随机
     */
    private fun generateSweepPackets(): List<ElementDamagePacket> {
        val attributeMap = user.attributeMap
        val list = arrayListOf<ElementDamagePacket>()
        for (it in ElementRegistry.INSTANCES.objects) {
            list.add(
                ElementDamagePacket(
                    it,
                    1.0,
                    1.0,
                    attributeMap.getValue(Attributes.byElement(it).ATTACK_DAMAGE_RATE),
                    attributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER),
                    Random.nextDouble() < attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE),
                    attributeMap.getValue(Attributes.byElement(it).DEFENSE_PENETRATION)
                )
            )
        }
        return list
    }


    private fun generateNotSweepPackets(): List<ElementDamagePacket> {
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
                    Random.nextDouble() < attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE),
                    attributeMap.getValue(Attributes.byElement(it).DEFENSE_PENETRATION)
                )
            )
        }
        return list
    }
}

/**
 * 非玩家实体近战攻击造成的伤害元数据
 * 如：僵尸近战攻击、河豚接触伤害
 */
class EntityMeleeAttackMetaData(
    entity: LivingEntity
) : DamageMetaData {
    override val packets: List<ElementDamagePacket>
        get() = TODO("Not yet implemented")
    override val damageValue: Double = TODO("实体伤害的获取和计算")
}

/**
 * 玩家使用弹射物造成伤害的元数据
 * 如：玩家射出的箭矢、三叉戟击中实体
 * 对于箭矢，除了计算玩家身上已有的属性值外，还需额外加上箭矢的属性
 * 对于三叉戟，则不需要。因为三叉戟投掷命中和直接近战击打没有区别
 */
class PlayerProjectileMetaData(
    val user: User<Player>,
    val projectileType: ProjectileType,
    val nekoStack: PlayNekoStack
) : DamageMetaData {
    override val packets: List<ElementDamagePacket> = generatePackets(projectileType)
    override val damageValue: Double = packets.sumOf { it.finalDamage }

    private fun generatePackets(projectileType: ProjectileType): List<ElementDamagePacket> {
        return when (projectileType) {
            ProjectileType.ARROW -> generateArrowPackets()
            ProjectileType.TRIDENT -> generateTridentPackets()
        }
    }

    private fun generateArrowPackets(): List<ElementDamagePacket> {
        //TODO 获取箭矢的属性
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
                    Random.nextDouble() < attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE),
                    attributeMap.getValue(Attributes.byElement(it).DEFENSE_PENETRATION)
                )
            )
        }
        return list
    }

    private fun generateTridentPackets(): List<ElementDamagePacket> {
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
                    Random.nextDouble() < attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE),
                    attributeMap.getValue(Attributes.byElement(it).DEFENSE_PENETRATION)
                )
            )
        }
        return list
    }
}

/**
 * 非玩家实体使用弹射物造成伤害的元数据
 * 如：骷髅射出的箭矢、溺尸射出的三叉戟、监守者的音爆击中实体
 */
class EntityProjectileMetaData(
    entity: LivingEntity
) : DamageMetaData {
    override val packets: List<ElementDamagePacket>
        get() = TODO("Not yet implemented")
    override val damageValue: Double = TODO("实体弹射物攻击的获取和计算")
}

data class ElementDamagePacket(
    val element: Element,
    val min: Double,
    val max: Double,
    val rate: Double,
    val criticalPower: Double,
    val isCritical: Boolean,
    val defensePenetration: Double,
) {
    val value: Double = if (min >= max) max else VariableAmount.range(min, max).amount

    /**
     * 最终伤害 = Σ random(MIN_ATTACK_DAMAGE, MAX_ATTACK_DAMAGE) * (1 + ATTACK_DAMAGE_RATE) * (1 + CRITICAL_STRIKE_POWER)
     */
    val finalDamage: Double = value * (1 + rate) * (1.0 + if (isCritical) criticalPower else 0.0)
}

/**
 * 弹射物类型
 * TODO 考虑风弹、雪球等所有原版弹射物
 */
enum class ProjectileType {
    ARROW, TRIDENT
}
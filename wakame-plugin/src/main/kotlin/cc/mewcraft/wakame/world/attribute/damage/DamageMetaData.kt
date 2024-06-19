package cc.mewcraft.wakame.world.attribute.damage

import cc.mewcraft.wakame.attribute.AttributeMap
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.binary.getMetaAccessor
import cc.mewcraft.wakame.item.binary.meta.BArrowMeta
import cc.mewcraft.wakame.item.binary.playNekoStack
import cc.mewcraft.wakame.item.binary.playNekoStackOrNull
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.user.User
import me.lucko.helper.random.VariableAmount
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
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
 * 默认伤害元数据
 * 用于处理各种繁杂但一般不需要经过wakame属性系统计算的伤害
 * 如：溺水伤害，风弹、末影珍珠击中实体伤害
 * 不做任何处理，只包含伤害值这一信息，伤害元素类型为默认元素
 */
class DefaultDamageMetaData(
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
 * 原版伤害元数据
 * 用于特定原版伤害类型加上元素和护甲穿透
 * 如：给溺水伤害加上水元素，给着火伤害加上火元素
 */
class VanillaDamageMetaData(
    override val damageValue: Double,
    val element: Element,
    val defensePenetration: Double
) : DamageMetaData {
    override val packets: List<ElementDamagePacket> = listOf(generateVanillaPacket())

    private fun generateVanillaPacket(): ElementDamagePacket {
        return ElementDamagePacket(
            element,
            damageValue,
            damageValue,
            0.0,
            0.0,
            false,
            defensePenetration
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

    /**
     * 在元素伤害包生成时，所有的随机就已经确定了，包括：
     * 伤害值 value 在范围 [max,min] 的随机
     * 该元素伤害是否暴击的随机
     */
    private fun generatePackets(isSweep: Boolean): List<ElementDamagePacket> {
        val attributeMap = user.attributeMap
        if (isSweep) {
            val list = mutableListOf<ElementDamagePacket>()
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
        } else {
            return generatePackets0(attributeMap)
        }
    }
}

/**
 * 非玩家实体近战攻击造成的伤害元数据
 * 如：僵尸近战攻击、河豚接触伤害
 */
class EntityMeleeAttackMetaData(
    entity: LivingEntity
) : DamageMetaData {
    override val packets: List<ElementDamagePacket> = TODO()
    override val damageValue: Double = packets.sumOf { it.finalDamage }
}

sealed interface ProjectileDamageMetaData : DamageMetaData

/**
 * 玩家使用弹射物造成伤害的元数据
 * 如：玩家射出的箭矢、三叉戟击中实体
 * 对于箭矢，除了计算玩家身上已有的属性值外，还需额外加上箭矢的属性
 * 对于三叉戟，则不需要，因为三叉戟投掷命中和直接近战击打没有区别
 */
class PlayerProjectileDamageMetaData(
    val user: User<Player>,
    val projectileType: ProjectileType,
    val itemStack: ItemStack
) : ProjectileDamageMetaData {
    override val packets: List<ElementDamagePacket> = generatePackets(projectileType)
    override val damageValue: Double = packets.sumOf { it.finalDamage }

    private fun generatePackets(projectileType: ProjectileType): List<ElementDamagePacket> {
        val attributeMap = user.attributeMap
        when (projectileType) {
            ProjectileType.ARROWS -> {
                val nekoStack = itemStack.playNekoStack
                //如果玩家射出的箭矢
                //不是nekoStack，则为原版箭矢
                //没有arrowMeta，视为原版箭矢，理论上不应该出现这种情况
                if (!nekoStack.isNeko || !nekoStack.getMetaAccessor<BArrowMeta>().exists) {
                    return generatePackets0(attributeMap)
                } else {
                    //将箭矢上的属性加到玩家身上
                    val itemModifiers = nekoStack.cell.getAttributeModifiers()
                    itemModifiers.forEach { attribute, modifier -> attributeMap.getInstance(attribute)?.addModifier(modifier) }
                    val elementDamagePackets = generatePackets0(attributeMap)
                    //生成完伤害包以后移除掉附加的属性
                    itemModifiers.forEach { attribute, modifier -> attributeMap.getInstance(attribute)?.removeModifier(modifier) }
                    return elementDamagePackets
                }
            }

            ProjectileType.TRIDENT -> {
                return generatePackets0(attributeMap)
            }
        }
    }
}

/**
 * 非玩家实体使用弹射物造成伤害的元数据
 * 如：骷髅射出的箭矢、溺尸射出的三叉戟
 */
class EntityProjectileDamageMetaData(
    entity: LivingEntity,
    val projectileType: ProjectileType
) : ProjectileDamageMetaData {
    override val packets: List<ElementDamagePacket> = TODO()
    override val damageValue: Double = packets.sumOf { it.finalDamage }
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

    //最终伤害 = Σ random(MIN_ATTACK_DAMAGE, MAX_ATTACK_DAMAGE) * (1 + ATTACK_DAMAGE_RATE) * (1 + CRITICAL_STRIKE_POWER)
    val finalDamage: Double = value * (1 + rate) * (1.0 + if (isCritical) criticalPower else 0.0)
}

/**
 * 弹射物类型
 * 包含所有会经过wakame属性系统处理的弹射物
 * 其他原版弹射物不应该在此枚举类出现，而应该由 [DamageManager] 过滤，并视为原版伤害
 * 如风弹砸生物造成的1伤害，应采用 [DefaultDamageMetaData] 而非 [ProjectileDamageMetaData]
 */
enum class ProjectileType {
    /**
     * 箭矢（普通箭、光灵箭、药水箭）
     */
    ARROWS,

    /**
     * 三叉戟
     */
    TRIDENT
}

/**
 * 用于简化重复的代码
 */
private fun generatePackets0(attributeMap: AttributeMap): List<ElementDamagePacket> {
    val list = mutableListOf<ElementDamagePacket>()
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
package cc.mewcraft.wakame.world.attribute.damage

import cc.mewcraft.wakame.attribute.AttributeMap
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.attribute.EntityAttributeAccessor
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.user.User
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

/**
 * 伤害元数据
 * 包含了一次伤害中“攻击阶段”的有关信息
 * 其实现类实例化时，攻击伤害值以及各种信息就已经确定了
 */
sealed interface DamageMetadata {
    val packets: List<DamagePacket>
        get() = generatePackets()
    val damageValue: Double
    val criticalPower: Double
    val isCritical: Boolean

    fun generatePackets(): List<DamagePacket>
}

/**
 * 默认伤害元数据
 * 用于处理各种繁杂但一般不需要经过wakame属性系统计算的伤害
 * 如：溺水伤害，风弹、末影珍珠击中实体伤害
 * 不做任何处理，只包含伤害值这一信息，伤害元素类型为默认元素
 */
class DefaultDamageMetadata(
    override val damageValue: Double,
) : DamageMetadata {
    override val criticalPower: Double = 1.0
    override val isCritical: Boolean = false

    override fun generatePackets(): List<DamagePacket> {
        return listOf(
            DamagePacket(
                ElementRegistry.DEFAULT,
                damageValue,
                damageValue,
                0.0,
                0.0,
                0.0
            )
        )
    }
}

/**
 * 原版伤害元数据
 * 用于特定原版伤害类型加上元素和护甲穿透
 * 如：给溺水伤害加上水元素，给着火伤害加上火元素
 */
class VanillaDamageMetadata(
    override val damageValue: Double,
    val element: Element,
    private val defensePenetration: Double,
    private val defensePenetrationRate: Double,
) : DamageMetadata {
    override val criticalPower: Double = 1.0
    override val isCritical: Boolean = false
    override fun generatePackets(): List<DamagePacket> {
        return listOf(
            DamagePacket(
                element,
                damageValue,
                damageValue,
                0.0,
                defensePenetration,
                defensePenetrationRate
            )
        )
    }
}

/**
 * 玩家近战攻击造成的伤害元数据
 * 如：玩家使用剑攻击生物
 * [isSweep] 表示这次伤害是否是横扫造成的
 * 会根据横扫的逻辑削弱伤害
 */
class PlayerMeleeAttackMetadata(
    val user: User<Player>,
    private val isSweep: Boolean,
) : DamageMetadata {
    override val damageValue: Double = packets.sumOf { it.packetDamage }
    override val criticalPower: Double = user.attributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER)
    override val isCritical: Boolean = Random.nextDouble() < user.attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE)
    override fun generatePackets(): List<DamagePacket> {
        val attributeMap = user.attributeMap
        if (isSweep) {
            val list = mutableListOf<DamagePacket>()
            for (it in ElementRegistry.INSTANCES.values) {
                list.add(
                    DamagePacket(
                        it,
                        1.0,
                        1.0,
                        attributeMap.getValue(Attributes.byElement(it).ATTACK_DAMAGE_RATE)
                                + attributeMap.getValue(Attributes.UNIVERSAL_ATTACK_DAMAGE_RATE),
                        attributeMap.getValue(Attributes.byElement(it).DEFENSE_PENETRATION)
                                + attributeMap.getValue(Attributes.UNIVERSAL_DEFENSE_PENETRATION),
                        attributeMap.getValue(Attributes.byElement(it).DEFENSE_PENETRATION_RATE)
                                + attributeMap.getValue(Attributes.UNIVERSAL_DEFENSE_PENETRATION_RATE)
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
class EntityMeleeAttackMetadata(
    entity: LivingEntity,
) : DamageMetadata {
    private val entityAttributeMap = EntityAttributeAccessor.getAttributeMap(entity)
    override val damageValue: Double = packets.sumOf { it.packetDamage }
    override val criticalPower: Double = entityAttributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER)
    override val isCritical: Boolean = Random.nextDouble() < entityAttributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE)
    override fun generatePackets(): List<DamagePacket> {
        return generatePackets0(entityAttributeMap)
    }
}

sealed interface ProjectileDamageMetadata : DamageMetadata {
    val projectileType: ProjectileType
}

/**
 * 玩家使用弹射物造成伤害的元数据
 * 如：玩家射出的箭矢、三叉戟击中实体
 * 对于箭矢，除了计算玩家身上已有的属性值外，还需额外加上箭矢的属性
 * 对于三叉戟，则不需要，因为三叉戟投掷命中和直接近战击打没有区别
 */
class PlayerProjectileDamageMetadata(
    override val projectileType: ProjectileType,
    val user: User<Player>,
    val itemStack: ItemStack,
    private val force: Float,
) : ProjectileDamageMetadata {
    override val damageValue: Double = packets.sumOf { it.packetDamage }
    override val criticalPower: Double = user.attributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER)
    override val isCritical: Boolean = Random.nextDouble() < user.attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE)

    override fun generatePackets(): List<DamagePacket> {
        val attributeMap = user.attributeMap
        when (projectileType) {
            ProjectileType.ARROWS -> {
                var isCustomArrow = true
                // 如果玩家射出的箭矢
                // 不是nekoStack，则为原版箭矢
                val nekoStack = itemStack.tryNekoStack ?: return generatePackets0(attributeMap)

                // 没有ARROW组件，视为原版箭矢，理论上不应该出现这种情况
                if (!nekoStack.components.has(ItemComponentTypes.ARROW)) {
                    return generatePackets0(attributeMap)
                }

                // 没有CELLS组件，视为原版箭矢
                val cells = nekoStack.components.get(ItemComponentTypes.CELLS) ?: return generatePackets0(attributeMap)

                // 将箭矢上的属性加到玩家身上
                val attributeModifiers = cells.collectAttributeModifiers(nekoStack, true)
                attributeModifiers.forEach { attribute, modifier ->
                    attributeMap.getInstance(attribute)?.addModifier(modifier)
                }

                // 生成伤害包，注意箭矢的伤害与拉弓的力度有关
                val damagePackets = mutableListOf<DamagePacket>()
                for (it in ElementRegistry.INSTANCES.values) {
                    damagePackets.add(
                        DamagePacket(
                            it,
                            force * (attributeMap.getValue(Attributes.byElement(it).MIN_ATTACK_DAMAGE)
                                    + attributeMap.getValue(Attributes.UNIVERSAL_MIN_ATTACK_DAMAGE)),
                            force * (attributeMap.getValue(Attributes.byElement(it).MAX_ATTACK_DAMAGE)
                                    + attributeMap.getValue(Attributes.UNIVERSAL_MAX_ATTACK_DAMAGE)),
                            attributeMap.getValue(Attributes.byElement(it).ATTACK_DAMAGE_RATE)
                                    + attributeMap.getValue(Attributes.UNIVERSAL_ATTACK_DAMAGE_RATE),
                            attributeMap.getValue(Attributes.byElement(it).DEFENSE_PENETRATION)
                                    + attributeMap.getValue(Attributes.UNIVERSAL_DEFENSE_PENETRATION),
                            attributeMap.getValue(Attributes.byElement(it).DEFENSE_PENETRATION_RATE)
                                    + attributeMap.getValue(Attributes.UNIVERSAL_DEFENSE_PENETRATION_RATE)
                        )
                    )
                }

                // 生成完伤害包以后移除掉附加的属性
                attributeModifiers.forEach { attribute, modifier ->
                    attributeMap.getInstance(attribute)?.removeModifier(modifier)
                }
                return damagePackets
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
class EntityProjectileDamageMetadata(
    override val projectileType: ProjectileType,
    val entity: LivingEntity,
) : ProjectileDamageMetadata {
    private val entityAttributeMap = EntityAttributeAccessor.getAttributeMap(entity)
    override val damageValue: Double = packets.sumOf { it.packetDamage }
    override val criticalPower: Double = entityAttributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER)
    override val isCritical: Boolean = Random.nextDouble() < entityAttributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE)
    override fun generatePackets(): List<DamagePacket> {
        return generatePackets0(entityAttributeMap)
    }
}

/**
 * 自定义伤害的元数据
 * 如：技能造成的伤害
 */
class CustomDamageMetadata(
    override val criticalPower: Double,
    override val isCritical: Boolean,
    val knockback: Boolean,
    private val customDamagePackets: List<DamagePacket>,
) : DamageMetadata {
    override val damageValue: Double = packets.sumOf { it.packetDamage }
    override fun generatePackets(): List<DamagePacket> {
        return customDamagePackets
    }
}


/**
 * 弹射物类型
 * 包含所有会经过wakame属性系统处理的弹射物
 * 其他原版弹射物不应该在此枚举类出现，而应该由 [DamageManager] 过滤，并视为原版伤害
 * 如风弹砸生物造成的1伤害，应采用 [DefaultDamageMetadata] 而非 [ProjectileDamageMetadata]
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
private fun generatePackets0(attributeMap: AttributeMap): List<DamagePacket> {
    val list = mutableListOf<DamagePacket>()
    for (it in ElementRegistry.INSTANCES.values) {
        list.add(
            DamagePacket(
                it,
                attributeMap.getValue(Attributes.byElement(it).MIN_ATTACK_DAMAGE)
                        + attributeMap.getValue(Attributes.UNIVERSAL_MIN_ATTACK_DAMAGE),
                attributeMap.getValue(Attributes.byElement(it).MAX_ATTACK_DAMAGE)
                        + attributeMap.getValue(Attributes.UNIVERSAL_MAX_ATTACK_DAMAGE),
                attributeMap.getValue(Attributes.byElement(it).ATTACK_DAMAGE_RATE)
                        + attributeMap.getValue(Attributes.UNIVERSAL_ATTACK_DAMAGE_RATE),
                attributeMap.getValue(Attributes.byElement(it).DEFENSE_PENETRATION)
                        + attributeMap.getValue(Attributes.UNIVERSAL_DEFENSE_PENETRATION),
                attributeMap.getValue(Attributes.byElement(it).DEFENSE_PENETRATION_RATE)
                        + attributeMap.getValue(Attributes.UNIVERSAL_DEFENSE_PENETRATION_RATE)
            )
        )
    }
    return list
}

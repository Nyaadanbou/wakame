package cc.mewcraft.wakame.world.attribute.damage

import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.attribute.EntityAttributeAccessor
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.tryNekoStack
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
    /**
     * 伤害捆绑包
     * 包含了这次伤害中各元素伤害值的信息
     */
    val damageBundle: DamageBundle

    /**
     * 攻击阶段伤害的终值
     */
    val damageValue: Double

    /**
     * 这次伤害暴击时暴击倍率的值
     */
    val criticalPower: Double

    /**
     * 这次伤害是否暴击
     */
    val isCritical: Boolean
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
    override val damageBundle: DamageBundle = damageBundle {
        default {
            min(damageValue)
            max(damageValue)
            rate(0.0)
            defensePenetration(0.0)
            defensePenetrationRate(0.0)
        }
    }
    override val criticalPower: Double = 1.0
    override val isCritical: Boolean = false
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
    override val damageBundle: DamageBundle = damageBundle {
        default {
            min(damageValue)
            max(damageValue)
            rate(0.0)
            defensePenetration(defensePenetration)
            defensePenetrationRate(defensePenetrationRate)
        }
    }
    override val criticalPower: Double = 1.0
    override val isCritical: Boolean = false
}

/**
 * 玩家近战攻击造成的伤害元数据
 * 如：玩家使用剑攻击生物
 * [isSweep] 表示这次伤害是否是横扫造成的
 * 会根据横扫的逻辑削弱伤害
 */
class PlayerMeleeAttackMetadata(
    val user: User<Player>,
    private val isSweep: Boolean
) : DamageMetadata {
    private val attributeMap = user.attributeMap
    override val damageBundle: DamageBundle = buildDamageBundle()
    override val damageValue: Double = damageBundle.bundleDamage
    override val criticalPower: Double = attributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER)
    override val isCritical: Boolean = Random.nextDouble() < attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE)

    private fun buildDamageBundle(): DamageBundle {
        return if (isSweep) {
            damageBundle(attributeMap) {
                every {
                    min(1.0)
                    max(1.0)
                    rate { standard() }
                    defensePenetration { standard() }
                    defensePenetrationRate { standard() }
                }
            }
        } else {
            damageBundle(attributeMap) {
                every { standard() }
            }
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
    override val damageBundle: DamageBundle = damageBundle(entityAttributeMap) {
        every { standard() }
    }
    override val damageValue: Double = damageBundle.bundleDamage
    override val criticalPower: Double = entityAttributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER)
    override val isCritical: Boolean = Random.nextDouble() < entityAttributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE)
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
    private val attributeMap = user.attributeMap
    override val damageBundle: DamageBundle = buildDamageBundle()
    override val damageValue: Double = damageBundle.bundleDamage
    override val criticalPower: Double = attributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER)
    override val isCritical: Boolean = Random.nextDouble() < attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE)

    private fun buildBowDamageBundle(): DamageBundle {
        //AttributeMap需要实时获取
        return damageBundle(attributeMap) {
            every {
                standard()
                min { force * standard() }
                max { force * standard() }
            }
        }
    }

    private fun buildDamageBundle(): DamageBundle {
        when (projectileType) {
            ProjectileType.ARROWS -> {
                // 如果玩家射出的箭矢
                // 不是nekoStack，则为原版箭矢
                val nekoStack = itemStack.tryNekoStack ?: return buildBowDamageBundle()

                // 没有ARROW组件，视为原版箭矢，理论上不应该出现这种情况
                if (!nekoStack.components.has(ItemComponentTypes.ARROW)) {
                    return buildBowDamageBundle()
                }

                // 没有CELLS组件，视为原版箭矢
                val cells = nekoStack.components.get(ItemComponentTypes.CELLS) ?: return buildBowDamageBundle()

                // 将箭矢上的属性加到玩家身上
                val attributeModifiers = cells.collectAttributeModifiers(nekoStack, true)
                attributeModifiers.forEach { attribute, modifier ->
                    attributeMap.getInstance(attribute)?.addModifier(modifier)
                }

                // 生成伤害包
                val damageBundle = buildBowDamageBundle()

                // 生成完伤害包以后移除掉附加的属性
                attributeModifiers.forEach { attribute, modifier ->
                    attributeMap.getInstance(attribute)?.removeModifier(modifier)
                }

                return damageBundle
            }

            ProjectileType.TRIDENT -> {
                return damageBundle(attributeMap) {
                    every { standard() }
                }
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
    override val damageBundle: DamageBundle = damageBundle(entityAttributeMap) {
        every { standard() }
    }
    override val damageValue: Double = damageBundle.bundleDamage
    override val criticalPower: Double = entityAttributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER)
    override val isCritical: Boolean = Random.nextDouble() < entityAttributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE)
}

/**
 * 自定义伤害的元数据
 * 如：技能造成的伤害
 */
class CustomDamageMetadata(
    override val criticalPower: Double,
    override val isCritical: Boolean,
    val knockback: Boolean,
    override val damageBundle: DamageBundle
) : DamageMetadata {
    override val damageValue: Double = damageBundle.bundleDamage
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
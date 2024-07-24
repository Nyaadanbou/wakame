package cc.mewcraft.wakame.world.attribute.damage

import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.attribute.EntityAttributeAccessor
import cc.mewcraft.wakame.attribute.IntangibleAttributeMap
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.user.User
import org.bukkit.entity.*
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
 * 原版伤害元数据
 * 默认的伤害元数据，作为缺省值存在
 * 可用于给特定原版伤害类型加上元素和护甲穿透
 * 如：给溺水伤害加上水元素，给着火伤害加上火元素
 */
class VanillaDamageMetadata(
    override val damageValue: Double,
    val element: Element,
    private val defensePenetration: Double,
    private val defensePenetrationRate: Double,
) : DamageMetadata {
    override val damageBundle: DamageBundle = damageBundle {
        single(element) {
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
    override val damageBundle: DamageBundle = buildDamageBundle()
    override val damageValue: Double = damageBundle.bundleDamage
    override val criticalPower: Double = user.attributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER)
    override val isCritical: Boolean = Random.nextDouble() < user.attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE)

    private fun buildDamageBundle(): DamageBundle {
        return if (isSweep) {
            damageBundle(user.attributeMap) {
                every {
                    min(1.0)
                    max(1.0)
                    rate { standard() }
                    defensePenetration { standard() }
                    defensePenetrationRate { standard() }
                }
            }
        } else {
            damageBundle(user.attributeMap) {
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
    val entity: LivingEntity,
) : DamageMetadata {
    override val damageBundle: DamageBundle = damageBundle(EntityAttributeAccessor.getAttributeMap(entity)) {
        every { standard() }
    }
    override val damageValue: Double = damageBundle.bundleDamage
    override val criticalPower: Double = EntityAttributeAccessor.getAttributeMap(entity).getValue(Attributes.CRITICAL_STRIKE_POWER)
    override val isCritical: Boolean = Random.nextDouble() < EntityAttributeAccessor.getAttributeMap(entity).getValue(Attributes.CRITICAL_STRIKE_CHANCE)
}

sealed interface ProjectileDamageMetadata : DamageMetadata {
    val projectile: Projectile
}

/**
 * 玩家使用弓、弩射出箭矢造成伤害的元数据
 * 箭矢除了计算玩家身上已有的属性值外
 * 还需额外加上箭矢的属性，并计算拉弓力度
 */
class PlayerArrowDamageMetadata private constructor(
    val user: User<Player>,
    override val projectile: AbstractArrow,
    private val force: Float,
) : ProjectileDamageMetadata {
    constructor(user: User<Player>, arrow: Arrow, force: Float) : this(user, arrow as AbstractArrow, force)
    constructor(user: User<Player>, arrow: SpectralArrow, force: Float) : this(user, arrow as AbstractArrow, force)

    override val damageBundle: DamageBundle = buildDamageBundle()
    override val damageValue: Double = damageBundle.bundleDamage
    override val criticalPower: Double = user.attributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER)
    override val isCritical: Boolean = Random.nextDouble() < user.attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE)

    private fun defaultBowArrowDamageBundle(): DamageBundle {
        return damageBundle(user.attributeMap) {
            every {
                standard()
                min { force * standard() }
                max { force * standard() }
            }
        }
    }

    private fun buildDamageBundle(): DamageBundle {
        val userAttributeMap = user.attributeMap
        val itemStack = projectile.itemStack
        // 如果玩家射出的箭矢
        // 不是nekoStack，则为原版箭矢
        val nekoStack = itemStack.tryNekoStack ?: return defaultBowArrowDamageBundle()

        // 没有ARROW组件，视为原版箭矢，理论上不应该出现这种情况
        if (!nekoStack.components.has(ItemComponentTypes.ARROW)) {
            return defaultBowArrowDamageBundle()
        }

        // 没有CELLS组件，视为原版箭矢
        val cells = nekoStack.components.get(ItemComponentTypes.CELLS) ?: return defaultBowArrowDamageBundle()

        // 获取属性映射的快照，将箭矢的属性加上
        val attributeModifiers = cells.collectAttributeModifiers(nekoStack, true)
        val attributeMapSnapshot = userAttributeMap.getSnapshot()
        attributeModifiers.forEach { attribute, modifier ->
            attributeMapSnapshot.getInstance(attribute)?.addModifier(modifier)
        }

        return damageBundle(attributeMapSnapshot) {
            every {
                standard()
                min { force * standard() }
                max { force * standard() }
            }
        }
    }
}

/**
 * 玩家投掷三叉戟造成伤害的元数据
 */
class PlayerTridentDamageMetadata(
    val user: User<Player>,
    override val projectile: Trident,
) : ProjectileDamageMetadata {
    override val damageBundle: DamageBundle = damageBundle(user.attributeMap) {
        every { standard() }
    }
    override val damageValue: Double = damageBundle.bundleDamage
    override val criticalPower: Double = user.attributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER)
    override val isCritical: Boolean = Random.nextDouble() < user.attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE)
}

/**
 * 非玩家实体使用弹射物造成伤害的元数据
 * 如：骷髅射出的箭矢、溺尸射出的三叉戟
 * 这些弹射物的伤害完全取决于发射者的属性映射
 */
class EntityProjectileDamageMetadata(
    val entity: LivingEntity,
    override val projectile: Projectile
) : ProjectileDamageMetadata {
    override val damageBundle: DamageBundle = damageBundle(EntityAttributeAccessor.getAttributeMap(entity)) {
        every { standard() }
    }
    override val damageValue: Double = damageBundle.bundleDamage
    override val criticalPower: Double = EntityAttributeAccessor.getAttributeMap(entity).getValue(Attributes.CRITICAL_STRIKE_POWER)
    override val isCritical: Boolean = Random.nextDouble() < EntityAttributeAccessor.getAttributeMap(entity).getValue(Attributes.CRITICAL_STRIKE_CHANCE)
}

/**
 * 默认情况下箭矢造成伤害的元数据
 * 用于处理未记录伤害的箭矢等特殊情况
 * 如：箭矢落地后再次命中，发射器发射的箭矢命中
 */
class DefaultArrowDamageMetadata private constructor(
    override val projectile: AbstractArrow,
    intangibleAttributeMap: IntangibleAttributeMap
) : ProjectileDamageMetadata {
    constructor(arrow: Arrow, intangibleAttributeMap: IntangibleAttributeMap) : this(arrow as AbstractArrow, intangibleAttributeMap)
    constructor(arrow: SpectralArrow, intangibleAttributeMap: IntangibleAttributeMap) : this(arrow as AbstractArrow, intangibleAttributeMap)

    override val damageBundle: DamageBundle = buildDamageBundle(intangibleAttributeMap)
    override val damageValue: Double = damageBundle.bundleDamage
    override val criticalPower: Double = 1.0
    override val isCritical: Boolean = false

    private fun defaultArrowDamageBundle(): DamageBundle {
        return damageBundle {
            every {
                min(1.0)
                max(1.0)
                rate(0.0)
                defensePenetration(0.0)
                defensePenetrationRate(0.0)
            }
        }
    }

    private fun buildDamageBundle(intangibleAttributeMap: IntangibleAttributeMap): DamageBundle {
        val itemStack = projectile.itemStack
        // 不是nekoStack，则为原版箭矢
        val nekoStack = itemStack.tryNekoStack ?: return defaultArrowDamageBundle()

        // 没有ARROW组件，视为原版箭矢，理论上不应该出现这种情况
        if (!nekoStack.components.has(ItemComponentTypes.ARROW)) {
            return defaultArrowDamageBundle()
        }

        // 没有CELLS组件，视为原版箭矢
        val cells = nekoStack.components.get(ItemComponentTypes.CELLS) ?: return defaultArrowDamageBundle()

        // 获取无形属性映射的快照，将箭矢的属性加上
        val attributeModifiers = cells.collectAttributeModifiers(nekoStack, true)
        val attributeMapSnapshot = intangibleAttributeMap.getSnapshot()
        attributeModifiers.forEach { attribute, modifier ->
            attributeMapSnapshot.getInstance(attribute)?.addModifier(modifier)
        }

        return damageBundle(attributeMapSnapshot) {
            every { standard() }
        }
    }
}

/**
 * 默认情况下三叉戟造成伤害的元数据
 * 用于处理未记录伤害的三叉戟等特殊情况
 * 如：三叉戟落地后再次命中（原版无此特性）
 */
class DefaultTridentDamageMetadata(
    override val projectile: Trident,
    intangibleAttributeMap: IntangibleAttributeMap
) : ProjectileDamageMetadata {
    override val damageBundle: DamageBundle = buildDamageBundle(intangibleAttributeMap)
    override val damageValue: Double = damageBundle.bundleDamage
    override val criticalPower: Double = 1.0
    override val isCritical: Boolean = false

    private fun defaultTridentDamageBundle(): DamageBundle {
        return damageBundle {
            every {
                min(8.0)
                max(8.0)
                rate(0.0)
                defensePenetration(0.0)
                defensePenetrationRate(0.0)
            }
        }
    }

    private fun buildDamageBundle(intangibleAttributeMap: IntangibleAttributeMap): DamageBundle {
        val itemStack = projectile.itemStack
        // 不是nekoStack，则为原版三叉戟
        val nekoStack = itemStack.tryNekoStack ?: return defaultTridentDamageBundle()

        // 没有CELLS组件，视为原版三叉戟
        val cells = nekoStack.components.get(ItemComponentTypes.CELLS) ?: return defaultTridentDamageBundle()

        // 获取无形属性映射的快照，将三叉戟的属性加上
        val attributeModifiers = cells.collectAttributeModifiers(nekoStack, true)
        val attributeMapSnapshot = intangibleAttributeMap.getSnapshot()
        attributeModifiers.forEach { attribute, modifier ->
            attributeMapSnapshot.getInstance(attribute)?.addModifier(modifier)
        }

        return damageBundle(attributeMapSnapshot) {
            every { standard() }
        }
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
    override val damageBundle: DamageBundle
) : DamageMetadata {
    override val damageValue: Double = damageBundle.bundleDamage
}
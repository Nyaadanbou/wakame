package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.attribute.*
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.user.User
import org.bukkit.entity.*
import java.lang.ref.WeakReference
import kotlin.math.absoluteValue
import kotlin.random.Random

/**
 * 伤害元数据, 包含了一次伤害中“攻击阶段”的有关信息.
 * 一旦实例化后, 攻击伤害的数值以及各种信息就已经确定了.
 */
sealed interface DamageMetadata {
    /**
     * 伤害捆绑包, 包含了这次伤害中各元素伤害值的信息.
     */
    val damageBundle: DamageBundle

    /**
     * 攻击阶段伤害的终值.
     */
    val damageValue: Double

    /**
     * 这次伤害暴击时暴击倍率的值. 小于1的值表示负暴击.
     */
    val criticalPower: Double

    /**
     * 这次伤害是否暴击.
     */
    val isCritical: Boolean
}

/**
 * 原版产生的伤害元数据.
 * 充当默认的伤害元数据, 作为缺省值存在.
 * 可用于给特定原版伤害类型加上元素和护甲穿透.
 * 如: 给溺水伤害加上水元素, 给着火伤害加上火元素.
 */
class VanillaDamageMetadata(
    override val damageValue: Double,
    element: Element,
    private val defensePenetration: Double,
    private val defensePenetrationRate: Double,
) : DamageMetadata {
    private val weakElement: WeakReference<Element> = WeakReference(element)

    /**
     * 这次伤害的元素类型.
     */
    val element: Element
        get() = weakElement.get() ?: throw IllegalStateException("Element reference no longer exists!")

    override val damageBundle: DamageBundle = damageBundle {
        single(element) {
            min(damageValue)
            max(damageValue)
            rate(1.0)
            defensePenetration(defensePenetration)
            defensePenetrationRate(defensePenetrationRate)
        }
    }
    override val criticalPower: Double = 1.0
    override val isCritical: Boolean = false
}

/**
 * 玩家近战攻击造成的伤害元数据.
 * 如: 玩家使用剑攻击生物.
 */
class PlayerMeleeAttackMetadata(
    user: User<Player>,
    /**
     * 这次伤害是否由横扫造成的, 会根据横扫的逻辑削弱伤害.
     */
    private val isSweep: Boolean,
) : DamageMetadata {
    private val weakUser: WeakReference<User<Player>> = WeakReference(user)

    /**
     * 造成这次伤害的玩家.
     */
    val user: User<Player>
        get() = weakUser.get() ?: throw IllegalStateException("User<Player> reference no longer exists!")

    override val damageBundle: DamageBundle = buildDamageBundle()
    override val damageValue: Double = damageBundle.damageSum
    override val criticalPower: Double =
        if (user.attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE) < 0) {
            user.attributeMap.getValue(Attributes.NEGATIVE_CRITICAL_STRIKE_POWER)
        } else {
            user.attributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER)
        }
    override val isCritical: Boolean = Random.nextDouble() < user.attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE).absoluteValue

    private fun buildDamageBundle(): DamageBundle {
        val attributeMap = user.attributeMap
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
 * 非玩家生物近战攻击造成的伤害元数据.
 * 如: 僵尸近战攻击、河豚接触伤害.
 */
class EntityMeleeAttackMetadata(
    entity: LivingEntity,
) : DamageMetadata {
    private val weakEntity: WeakReference<LivingEntity> = WeakReference(entity)

    /**
     * 造成这次伤害的生物.
     */
    val entity: LivingEntity
        get() = weakEntity.get() ?: throw IllegalStateException("LivingEntity reference no longer exists!")

    override val damageBundle: DamageBundle
    override val damageValue: Double
    override val criticalPower: Double
    override val isCritical: Boolean

    init {
        val attributeMap = EntityAttributeMapAccess.get(entity)
        this.damageBundle = damageBundle(attributeMap) { every { standard() } }
        this.damageValue = this.damageBundle.damageSum
        this.criticalPower =
            if (attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE) < 0) {
                attributeMap.getValue(Attributes.NEGATIVE_CRITICAL_STRIKE_POWER)
            } else {
                attributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER)
            }
        this.isCritical = Random.nextDouble() < attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE).absoluteValue
    }
}

sealed interface ProjectileDamageMetadata : DamageMetadata {
    /**
     * 造成这次伤害的弹射物.
     */
    val projectile: Projectile
}

/**
 * 玩家使用弓/弩射出箭矢的伤害元数据.
 * 箭矢除了计算玩家身上已有的属性值外,
 * 还需额外加上箭矢的属性, 并计算拉弓力度.
 */
class PlayerArrowDamageMetadata
private constructor(
    user: User<Player>,
    override val projectile: AbstractArrow,
    private val force: Float,
) : ProjectileDamageMetadata {
    constructor(user: User<Player>, arrow: Arrow, force: Float) : this(user, arrow as AbstractArrow, force)
    constructor(user: User<Player>, arrow: SpectralArrow, force: Float) : this(user, arrow as AbstractArrow, force)

    private val weakUser: WeakReference<User<Player>> = WeakReference(user)

    /**
     * 造成这次伤害的玩家.
     */
    val user: User<Player>
        get() = weakUser.get() ?: throw IllegalStateException("User<Player> reference no longer exists!")

    override val damageBundle: DamageBundle = buildDamageBundle()
    override val damageValue: Double = damageBundle.damageSum
    override val criticalPower: Double =
        if (user.attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE) < 0) {
            user.attributeMap.getValue(Attributes.NEGATIVE_CRITICAL_STRIKE_POWER)
        } else {
            user.attributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER)
        }
    override val isCritical: Boolean = Random.nextDouble() < user.attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE).absoluteValue

    private fun buildDefaultBowArrowDamageBundle(): DamageBundle {
        return damageBundle(user.attributeMap) {
            every {
                standard()
                min { force * standard() }
                max { force * standard() }
            }
        }
    }

    private fun buildDamageBundle(): DamageBundle {
        val playerAttributeMap = user.attributeMap
        val itemStack = projectile.itemStack
        // 如果玩家射出的箭矢
        // 不是 NekoStack, 则为原版箭矢
        val nekoStack = itemStack.tryNekoStack ?: return buildDefaultBowArrowDamageBundle()

        // 没有 ARROW 组件, 视为原版箭矢 (理论上不应该出现这种情况)
        if (!nekoStack.templates.has(ItemTemplateTypes.ARROW)) {
            return buildDefaultBowArrowDamageBundle()
        }

        // 没有 CELLS 组件, 视为原版箭矢
        val cells = nekoStack.components.get(ItemComponentTypes.CELLS) ?: return buildDefaultBowArrowDamageBundle()

        // 获取属性映射的快照, 将箭矢的属性加上
        val attributeModifiers = cells.collectAttributeModifiers(nekoStack, ItemSlot.imaginary())
        val attributeMapSnapshot = playerAttributeMap.getSnapshot()
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
    user: User<Player>,
    override val projectile: Trident,
) : ProjectileDamageMetadata {
    private val weakUser: WeakReference<User<Player>> = WeakReference(user)

    /**
     * 造成这次伤害的玩家.
     */
    val user: User<Player>
        get() = weakUser.get() ?: throw IllegalStateException("User<Player> reference no longer exists!")

    override val damageBundle: DamageBundle = damageBundle(user.attributeMap) { every { standard() } }
    override val damageValue: Double = damageBundle.damageSum
    override val criticalPower: Double =
        if (user.attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE) < 0) {
            user.attributeMap.getValue(Attributes.NEGATIVE_CRITICAL_STRIKE_POWER)
        } else {
            user.attributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER)
        }
    override val isCritical: Boolean = Random.nextDouble() < user.attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE).absoluteValue
}

/**
 * 非玩家实体使用弹射物造成的伤害元数据.
 * 如: 骷髅射出的箭矢、溺尸射出的三叉戟.
 * 这些弹射物的伤害完全取决于发射者的属性映射.
 */
class EntityProjectileDamageMetadata(
    entity: LivingEntity,
    override val projectile: Projectile,
) : ProjectileDamageMetadata {
    private val weakEntity: WeakReference<LivingEntity> = WeakReference(entity)

    /**
     * 造成这次伤害的生物.
     */
    val entity: LivingEntity
        get() = weakEntity.get() ?: throw IllegalStateException("LivingEntity reference no longer exists!")

    override val damageBundle: DamageBundle
    override val damageValue: Double
    override val criticalPower: Double
    override val isCritical: Boolean

    init {
        val attributeMap = EntityAttributeMapAccess.get(entity)
        this.damageBundle = damageBundle(attributeMap) { every { standard() } }
        this.damageValue = this.damageBundle.damageSum
        this.criticalPower =
            if (attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE) < 0) {
                attributeMap.getValue(Attributes.NEGATIVE_CRITICAL_STRIKE_POWER)
            } else {
                attributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER)
            }
        this.isCritical = Random.nextDouble() < attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE).absoluteValue
    }
}

/**
 * 默认情况下箭矢造成的伤害元数据.
 * 用于处理未记录伤害的箭矢等特殊情况.
 * 如: 箭矢落地后再次命中, 发射器发射的箭矢命中.
 */
class DefaultArrowDamageMetadata private constructor(
    imaginaryAttributeMap: ImaginaryAttributeMap,
    override val projectile: AbstractArrow,
) : ProjectileDamageMetadata {
    constructor(attributeMap: ImaginaryAttributeMap, arrow: Arrow) : this(attributeMap, arrow as AbstractArrow)
    constructor(attributeMap: ImaginaryAttributeMap, arrow: SpectralArrow) : this(attributeMap, arrow as AbstractArrow)

    override val damageBundle: DamageBundle = buildDamageBundle(imaginaryAttributeMap)
    override val damageValue: Double = damageBundle.damageSum
    override val criticalPower: Double = 1.0
    override val isCritical: Boolean = false

    private fun defaultArrowDamageBundle(): DamageBundle {
        return damageBundle {
            default {
                min(1.0)
                max(1.0)
                rate(1.0)
                defensePenetration(0.0)
                defensePenetrationRate(0.0)
            }
        }
    }

    private fun buildDamageBundle(imaginaryAttributeMap: ImaginaryAttributeMap): DamageBundle {
        val itemStack = projectile.itemStack

        // 不是 NekoStack, 则为原版箭矢
        val nekoStack = itemStack.tryNekoStack ?: return defaultArrowDamageBundle()

        // 没有 ARROW 组件, 视为原版箭矢, 理论上不应该出现这种情况
        if (!nekoStack.templates.has(ItemTemplateTypes.ARROW)) {
            return defaultArrowDamageBundle()
        }

        // 没有 CELLS 组件, 视为原版箭矢
        val cells = nekoStack.components.get(ItemComponentTypes.CELLS) ?: return defaultArrowDamageBundle()

        // 获取无形属性映射的快照, 将箭矢的属性加上
        val attributeModifiers = cells.collectAttributeModifiers(nekoStack, ItemSlot.imaginary())
        val attributeMapSnapshot = imaginaryAttributeMap.getSnapshot()
        attributeModifiers.forEach { attribute, modifier ->
            attributeMapSnapshot.getInstance(attribute)?.addModifier(modifier)
        }

        return damageBundle(attributeMapSnapshot) {
            every {
                standard()
            }
        }
    }
}

/**
 * 默认情况下三叉戟造成的伤害元数据.
 * 用于处理未记录伤害的三叉戟等特殊情况.
 * 如: 三叉戟落地后再次命中(原版无此特性).
 */
class DefaultTridentDamageMetadata(
    imaginaryAttributeMap: ImaginaryAttributeMap,
    override val projectile: Trident,
) : ProjectileDamageMetadata {
    override val damageBundle: DamageBundle = buildDamageBundle(imaginaryAttributeMap)
    override val damageValue: Double = damageBundle.damageSum
    override val criticalPower: Double = 1.0
    override val isCritical: Boolean = false

    private fun defaultTridentDamageBundle(): DamageBundle {
        return damageBundle {
            default {
                min(8.0)
                max(8.0)
                rate(1.0)
                defensePenetration(0.0)
                defensePenetrationRate(0.0)
            }
        }
    }

    private fun buildDamageBundle(imaginaryAttributeMap: ImaginaryAttributeMap): DamageBundle {
        val itemStack = projectile.itemStack

        // 不是 NekoStack, 则为原版三叉戟
        val nekoStack = itemStack.tryNekoStack ?: return defaultTridentDamageBundle()

        // 没有 CELLS 组件, 视为原版三叉戟
        val cells = nekoStack.components.get(ItemComponentTypes.CELLS) ?: return defaultTridentDamageBundle()

        // 获取无形属性映射的快照, 将三叉戟的属性加上
        val attributeModifiers = cells.collectAttributeModifiers(nekoStack, ItemSlot.imaginary())
        val attributeMapSnapshot = imaginaryAttributeMap.getSnapshot()
        attributeModifiers.forEach { attribute, modifier ->
            attributeMapSnapshot.getInstance(attribute)?.addModifier(modifier)
        }

        return damageBundle(attributeMapSnapshot) {
            every {
                standard()
            }
        }
    }
}

/**
 * 自定义的伤害元数据.
 * 如: 技能造成的伤害.
 */
class CustomDamageMetadata(
    override val criticalPower: Double,
    override val isCritical: Boolean,
    val knockback: Boolean,
    override val damageBundle: DamageBundle,
) : DamageMetadata {
    override val damageValue: Double = damageBundle.damageSum
}
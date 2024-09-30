package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.attribute.*
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.user.User
import org.bukkit.entity.*
import org.bukkit.projectiles.BlockProjectileSource
import org.koin.core.component.inject
import org.slf4j.Logger
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
     * 伤害标签集, 包含了这次伤害的特征标签.
     */
    val damageTags: DamageTags

    /**
     * 攻击阶段伤害的终值.
     */
    val damageValue: Double

    /**
     * 这次伤害暴击时暴击倍率的值.
     */
    val criticalPower: Double

    /**
     * 这次伤害的暴击状态.
     */
    val criticalState: CriticalState
}

/**
 * 暴击状态.
 */
enum class CriticalState {
    /**
     * 正暴击.
     */
    POSITIVE,

    /**
     * 负暴击.
     */
    NEGATIVE,

    /**
     * 无暴击.
     */
    NONE
}

private val logger: Logger by Injector.inject()

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
    override val damageTags: DamageTags = DamageTags()
    override val criticalPower: Double = 1.0
    override val criticalState: CriticalState = CriticalState.NONE
}

/**
 * 玩家近战攻击造成的伤害元数据.
 * 如: 玩家使用剑攻击生物.
 */
class PlayerMeleeAttackMetadata(
    user: User<Player>,
    override val damageTags: DamageTags
) : DamageMetadata {
    private val weakUser: WeakReference<User<Player>> = WeakReference(user)

    /**
     * 造成这次伤害的玩家.
     */
    val user: User<Player>
        get() = weakUser.get() ?: throw IllegalStateException("User<Player> reference no longer exists!")

    override val damageBundle: DamageBundle
    override val damageValue: Double
    override val criticalPower: Double
    override val criticalState: CriticalState

    init {
        val attributeMap = user.attributeMap
        this.damageBundle = buildDamageBundle()
        this.damageValue = damageBundle.damageSum
        val chance = attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE)
        if (chance < 0) {
            criticalPower = attributeMap.getValue(Attributes.NEGATIVE_CRITICAL_STRIKE_POWER)
            if (Random.nextDouble() < chance.absoluteValue) {
                this.criticalState = CriticalState.NEGATIVE
            } else {
                this.criticalState = CriticalState.NONE
            }
        } else {
            criticalPower = attributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER)
            if (Random.nextDouble() < chance) {
                this.criticalState = CriticalState.POSITIVE
            } else {
                this.criticalState = CriticalState.NONE
            }
        }
    }

    private fun buildDamageBundle(): DamageBundle {
        val attributeMap = user.attributeMap
        return if (damageTags.contains(DamageTag.EXTRA) && damageTags.contains(DamageTag.SWORD)) {
            // 是原版剑横扫攻击产生的范围伤害
            damageBundle(attributeMap) {
                every {
                    standard()
                    rate { standard() * attributeMap.getValue(Attributes.SWEEPING_DAMAGE_RATIO) }
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
    override val damageTags: DamageTags
    override val damageValue: Double
    override val criticalPower: Double
    override val criticalState: CriticalState

    init {
        val attributeMap = EntityAttributeMapAccess.get(entity).getOrElse {
            error("Failed to initialize EntityMeleeAttackMetadata because the entity does not have an attribute map: ${it.message}")
        }
        this.damageBundle = damageBundle(attributeMap) { every { standard() } }
        this.damageTags = DamageTags()
        this.damageValue = this.damageBundle.damageSum
        val chance = attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE)
        if (chance < 0) {
            criticalPower = attributeMap.getValue(Attributes.NEGATIVE_CRITICAL_STRIKE_POWER)
            if (Random.nextDouble() < chance.absoluteValue) {
                this.criticalState = CriticalState.NEGATIVE
            } else {
                this.criticalState = CriticalState.NONE
            }
        } else {
            criticalPower = attributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER)
            if (Random.nextDouble() < chance) {
                this.criticalState = CriticalState.POSITIVE
            } else {
                this.criticalState = CriticalState.NONE
            }
        }
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
    override val damageTags: DamageTags
) : ProjectileDamageMetadata {
    constructor(user: User<Player>, arrow: Arrow, force: Float, damageTags: DamageTags) : this(user, arrow as AbstractArrow, force, damageTags)
    constructor(user: User<Player>, arrow: SpectralArrow, force: Float, damageTags: DamageTags) : this(user, arrow as AbstractArrow, force, damageTags)

    private val weakUser: WeakReference<User<Player>> = WeakReference(user)

    /**
     * 造成这次伤害的玩家.
     */
    val user: User<Player>
        get() = weakUser.get() ?: throw IllegalStateException("User<Player> reference no longer exists!")

    override val damageBundle: DamageBundle
    override val damageValue: Double
    override val criticalPower: Double
    override val criticalState: CriticalState

    init {
        val attributeMap = user.attributeMap
        this.damageBundle = buildDamageBundle()
        this.damageValue = this.damageBundle.damageSum
        val chance = attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE)
        if (chance < 0) {
            criticalPower = attributeMap.getValue(Attributes.NEGATIVE_CRITICAL_STRIKE_POWER)
            if (Random.nextDouble() < chance.absoluteValue) {
                this.criticalState = CriticalState.NEGATIVE
            } else {
                this.criticalState = CriticalState.NONE
            }
        } else {
            criticalPower = attributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER)
            if (Random.nextDouble() < chance) {
                this.criticalState = CriticalState.POSITIVE
            } else {
                this.criticalState = CriticalState.NONE
            }
        }
    }

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
    override val damageTags: DamageTags
) : ProjectileDamageMetadata {
    private val weakUser: WeakReference<User<Player>> = WeakReference(user)

    /**
     * 造成这次伤害的玩家.
     */
    val user: User<Player>
        get() = weakUser.get() ?: throw IllegalStateException("User<Player> reference no longer exists!")

    override val damageBundle: DamageBundle
    override val damageValue: Double
    override val criticalPower: Double
    override val criticalState: CriticalState

    init {
        val attributeMap = user.attributeMap
        this.damageBundle = damageBundle(attributeMap) { every { standard() } }
        this.damageValue = this.damageBundle.damageSum
        val chance = attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE)
        if (chance < 0) {
            criticalPower = attributeMap.getValue(Attributes.NEGATIVE_CRITICAL_STRIKE_POWER)
            if (Random.nextDouble() < chance.absoluteValue) {
                this.criticalState = CriticalState.NEGATIVE
            } else {
                this.criticalState = CriticalState.NONE
            }
        } else {
            criticalPower = attributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER)
            if (Random.nextDouble() < chance) {
                this.criticalState = CriticalState.POSITIVE
            } else {
                this.criticalState = CriticalState.NONE
            }
        }
    }
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
    override val damageTags: DamageTags
    override val damageValue: Double
    override val criticalPower: Double
    override val criticalState: CriticalState

    init {
        val attributeMap = EntityAttributeMapAccess.get(entity).getOrElse {
            error("Failed to initialize EntityProjectileDamageMetadata because the entity does not have an attribute map: ${it.message}")
        }
        this.damageBundle = damageBundle(attributeMap) { every { standard() } }
        this.damageTags = DamageTags()
        this.damageValue = this.damageBundle.damageSum
        val chance = attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE)
        if (chance < 0) {
            criticalPower = attributeMap.getValue(Attributes.NEGATIVE_CRITICAL_STRIKE_POWER)
            if (Random.nextDouble() < chance.absoluteValue) {
                this.criticalState = CriticalState.NEGATIVE
            } else {
                this.criticalState = CriticalState.NONE
            }
        } else {
            criticalPower = attributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER)
            if (Random.nextDouble() < chance) {
                this.criticalState = CriticalState.POSITIVE
            } else {
                this.criticalState = CriticalState.NONE
            }
        }
    }
}

/**
 * 默认情况下弹射物造成的伤害元数据.
 * 用途:
 * 1.处理未记录伤害的箭矢等特殊情况.
 *   如: 箭矢落地后再次命中, 发射器发射的箭矢命中.
 * 2.处理未记录伤害的三叉戟等特殊情况.
 *   如: 三叉戟落地后再次命中(原版无此特性).
 */
class DefaultProjectileDamageMetadata(
    override val projectile: AbstractArrow,
) : ProjectileDamageMetadata {
    override val damageBundle: DamageBundle = buildDamageBundle()
    override val damageTags: DamageTags = DamageTags()
    override val damageValue: Double = damageBundle.damageSum
    override val criticalPower: Double = 1.0
    override val criticalState: CriticalState = CriticalState.NONE

    private fun buildDamageBundle(): DamageBundle {
        when (projectile) {
            is Arrow, is SpectralArrow -> {
                return buildArrowDamageBundle(
                    if (projectile.shooter is BlockProjectileSource) {
                        ImaginaryAttributeMaps.DISPENSER
                    } else {
                        ImaginaryAttributeMaps.ARROW
                    }
                )
            }

            is Trident -> {
                return buildTridentDamageBundle()
            }

            else -> {
                throw IllegalArgumentException("AbstractArrow is not Arrow, SpectralArrow, or Trident, so what is it?")
            }
        }
    }

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

    private fun buildArrowDamageBundle(imaginaryAttributeMap: ImaginaryAttributeMap): DamageBundle {
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

    private fun buildTridentDamageBundle(): DamageBundle {
        val itemStack = projectile.itemStack

        // 不是 NekoStack, 则为原版三叉戟
        val nekoStack = itemStack.tryNekoStack ?: return defaultTridentDamageBundle()

        // 没有 CELLS 组件, 视为原版三叉戟
        val cells = nekoStack.components.get(ItemComponentTypes.CELLS) ?: return defaultTridentDamageBundle()

        // 获取无形属性映射的快照, 将三叉戟的属性加上
        val attributeModifiers = cells.collectAttributeModifiers(nekoStack, ItemSlot.imaginary())
        val attributeMapSnapshot = ImaginaryAttributeMaps.TRIDENT.getSnapshot()
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
 * 如: 技能造成的伤害, 锤类武器的额外范围伤害
 */
class CustomDamageMetadata(
    override val criticalPower: Double,
    override val criticalState: CriticalState,
    val knockback: Boolean,
    override val damageBundle: DamageBundle,
    override val damageTags: DamageTags,
) : DamageMetadata {
    override val damageValue: Double = damageBundle.damageSum
}
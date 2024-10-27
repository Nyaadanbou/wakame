package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registry.ElementRegistry
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.user.toUser
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import java.lang.ref.WeakReference

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
     * 这次伤害的暴击元数据.
     */
    val criticalStrikeMetadata: CriticalStrikeMetadata
}

/**
 * 非生物造成的伤害元数据.
 * 用于给特定原版伤害类型加上元素和护甲穿透.
 * 如: 给溺水伤害加上水元素, 给着火伤害加上火元素.
 * 或用于无来源的弹射物的伤害.
 */
class VanillaDamageMetadata(
    override val damageBundle: DamageBundle,
) : DamageMetadata {
    constructor(element: Element, damageValue: Double, defensePenetration: Double, defensePenetrationRate: Double) : this(
        damageBundle = damageBundle {
            single(element) {
                min(damageValue)
                max(damageValue)
                rate(1.0)
                defensePenetration(defensePenetration)
                defensePenetrationRate(defensePenetrationRate)
            }
        }
    )

    constructor(damageValue: Double) : this(ElementRegistry.DEFAULT, damageValue, 0.0, 0.0)

    override val damageValue: Double = damageBundle.damageSum
    override val damageTags: DamageTags = DamageTags()
    override val criticalStrikeMetadata: CriticalStrikeMetadata = CriticalStrikeMetadata.DEFAULT
}

/**
 * 玩家造成的伤害元数据.
 */
class PlayerDamageMetadata(
    damager: Player,
    override val damageBundle: DamageBundle,
    override val damageTags: DamageTags,
) : DamageMetadata {
    companion object {
        /**
         * 玩家徒手造成的伤害.
         * 或相当于徒手攻击的物品造成的伤害.
         * 即 1 点默认元素伤害.
         */
        fun default(player: Player): PlayerDamageMetadata {
            return PlayerDamageMetadata(
                damager = player,
                damageTags = DamageTags(DamageTag.HAND),
                damageBundle = damageBundle {
                    default {
                        min(1.0)
                        max(1.0)
                        rate(1.0)
                        defensePenetration(.0)
                        defensePenetrationRate(.0)
                    }
                }
            )
        }
    }

    private val weakUser: WeakReference<User<Player>> = WeakReference(damager.toUser())

    /**
     * 造成这次伤害的玩家.
     */
    val user: User<Player>
        get() = weakUser.get() ?: throw IllegalStateException("User<Player> reference no longer exists!")

    override val damageValue: Double = damageBundle.damageSum
    override val criticalStrikeMetadata: CriticalStrikeMetadata = CriticalStrikeMetadata.byCalculate(
        chance = user.attributeMap.getValue(Attributes.CRITICAL_STRIKE_CHANCE),
        positivePower = user.attributeMap.getValue(Attributes.CRITICAL_STRIKE_POWER),
        negativePower = user.attributeMap.getValue(Attributes.NEGATIVE_CRITICAL_STRIKE_POWER)
    )
}

/**
 * 非玩家生物造成的伤害元数据.
 */
class EntityDamageMetadata(
    damager: LivingEntity,
    override val damageBundle: DamageBundle,
    override val criticalStrikeMetadata: CriticalStrikeMetadata,
) : DamageMetadata, KoinComponent {
    private val logger: Logger by inject()
    private val weakEntity: WeakReference<LivingEntity> = WeakReference(damager)

    init {
        if (damager is Player) {
            logger.warn("The damager of EntityDamageMetadata shouldn't be a player!")
        }
    }

    /**
     * 造成这次伤害的生物.
     */
    val entity: LivingEntity
        get() = weakEntity.get() ?: throw IllegalStateException("LivingEntity reference no longer exists!")

    override val damageTags: DamageTags = DamageTags()
    override val damageValue: Double = damageBundle.damageSum
}

/**
 * 自定义的伤害元数据.
 */
class CustomDamageMetadata(
    override val damageBundle: DamageBundle,
    override val damageTags: DamageTags,
    override val criticalStrikeMetadata: CriticalStrikeMetadata,
) : DamageMetadata {
    override val damageValue: Double = damageBundle.damageSum
}
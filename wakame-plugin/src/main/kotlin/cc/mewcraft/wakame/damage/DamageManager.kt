package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.attribute.EntityAttributeAccessor
import cc.mewcraft.wakame.attribute.IntangibleAttributeMaps
import cc.mewcraft.wakame.item.behavior.ItemBehaviorTypes
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.user.toUser
import com.github.benmanes.caffeine.cache.Caffeine
import org.bukkit.entity.*
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.projectiles.BlockProjectileSource
import java.time.Duration
import java.util.*

object DamageManager {
    fun generateDamageMetadata(event: EntityDamageEvent): DamageMetadata {
        // 先检查是不是自定义伤害; 若是, 则直接返回自定义伤害信息
        val uuid = event.entity.uniqueId
        val customDamageMetadata = findCustomDamageMetadata(uuid)
        if (customDamageMetadata != null) {
            if (!customDamageMetadata.knockback) {
                markCancelKnockback(uuid)
            }
            removeCustomDamageMetadata(uuid)
            return customDamageMetadata
        }

        // 不是自定义伤害, 并且存在造成伤害的实体时
        if (event is EntityDamageByEntityEvent) {
            when (val damager = event.damager) {
                // 造成伤害的是玩家
                is Player -> {
                    val itemStack = damager.inventory.itemInMainHand
                    // 玩家手中的物品是 Attack
                    if (itemStack.tryNekoStack?.behaviors?.has(ItemBehaviorTypes.ATTACK) == true) {
                        when (event.cause) {
                            DamageCause.ENTITY_ATTACK -> {
                                return PlayerMeleeAttackMetadata(damager.toUser(), false)
                            }

                            DamageCause.ENTITY_SWEEP_ATTACK -> {
                                return PlayerMeleeAttackMetadata(damager.toUser(), true)
                            }

                            else -> {}
                        }
                    }
                }

                // 造成伤害的是非玩家实体
                is LivingEntity -> {
                    return EntityMeleeAttackMetadata(damager)
                }

                // 造成伤害的是弹射物
                // 查找是否存在记录; 若不存在, 对箭矢和三叉戟进行特殊处理
                // 其他弹射物按照默认伤害处理
                is Projectile -> {
                    val projectileDamageMetadata = findProjectileDamageMetadata(damager.uniqueId)
                    if (projectileDamageMetadata != null) return projectileDamageMetadata
                    when (damager) {
                        is Arrow -> {
                            val attributeMap = if (damager.shooter is BlockProjectileSource) {
                                IntangibleAttributeMaps.DISPENSER
                            } else {
                                IntangibleAttributeMaps.ARROW
                            }
                            return DefaultArrowDamageMetadata(attributeMap, damager)
                        }

                        is SpectralArrow -> {
                            val attributeMap = if (damager.shooter is BlockProjectileSource) {
                                IntangibleAttributeMaps.DISPENSER
                            } else {
                                IntangibleAttributeMaps.ARROW
                            }
                            return DefaultArrowDamageMetadata(attributeMap, damager)
                        }

                        is Trident -> {
                            return DefaultTridentDamageMetadata(IntangibleAttributeMaps.TRIDENT, damager)
                        }
                    }
                }
            }
        }

        /*
        如果:
         - 伤害没有攻击者
         - 玩家使用无Attack行为物品甚至非neko物品攻击
         - 伤害攻击者非玩家、living实体、弹射物
         - 伤害攻击者是弹射物, 但没有记录, 也不是箭矢和三叉戟
        则视为原版伤害, 使用原版伤害映射
        */
        val damageMapping = VanillaDamageMappings.get(event.damageSource.damageType)
        return VanillaDamageMetadata(event.damage, damageMapping.element, damageMapping.defensePenetration, damageMapping.defensePenetrationRate)
    }

    fun generateDefenseMetadata(event: EntityDamageEvent): DefenseMetadata {
        return when (
            val damagee = event.entity
        ) {
            is Player -> {
                EntityDefenseMetadata(damagee.toUser().attributeMap)
            }

            is LivingEntity -> {
                EntityDefenseMetadata(EntityAttributeAccessor.getAttributeMap(damagee))
            }

            else -> {
                throw IllegalArgumentException("Damagee is not a living entity!")
            }
        }
    }

    private val projectileDamageMetadataMap = Caffeine.newBuilder()
        .softValues()
        .expireAfterAccess(Duration.ofSeconds(60))
        .build<UUID, ProjectileDamageMetadata>()

    /**
     * 弹射物生成时, 其伤害信息就应该确定了.
     * 只记录需要 wakame 属性系统处理的伤害信息.
     * 如玩家的风弹, 鸡蛋, 雪球, 末影珍珠等弹射物的伤害信息不会被记录, 将会使用 [VanillaDamageMetadata].
     * 伤害信息不存在时, 弹射物伤害将按原版处理.
     *
     * 伤害信息过期的情况如下 (满足其一):
     * - 超过有效期 (60秒)
     * - 弹射物击中方块
     */
    fun recordProjectileDamageMetadata(event: ProjectileLaunchEvent) {
        val projectile = event.entity
        when (
            val shooter = projectile.shooter
        ) {
            // 发射者是玩家时
            is Player -> {
                // 记录三叉戟伤害信息
                when (projectile) {
                    is Trident -> {
                        putProjectileDamageMetadata(
                            projectile.uniqueId,
                            PlayerTridentDamageMetadata(shooter.toUser(), projectile)
                        )
                    }

                    is Arrow, is SpectralArrow -> {
                        // 玩家箭矢的伤害信息需要拉弓力度
                        // 由 #recordProjectileDamageMetadata(event: EntityShootBowEvent) 处理
                    }
                }
            }

            // 发射者是非玩家实体时
            // 非玩家实体的弹射物伤害只和其AttributeMap有关
            is LivingEntity -> {
                putProjectileDamageMetadata(
                    projectile.uniqueId,
                    EntityProjectileDamageMetadata(shooter, projectile)
                )
            }
        }
    }

    fun recordProjectileDamageMetadata(event: EntityShootBowEvent) {
        val entity = event.entity
        if (entity !is Player) return

        val projectile = event.projectile
        // 玩家射出的箭矢伤害需要根据拉弓的力度进行调整
        val force = DamageRules.calculateBowForce(72000 - entity.itemUseRemainingTime)
        when (projectile) {
            is Arrow -> {
                putProjectileDamageMetadata(
                    projectile.uniqueId,
                    PlayerArrowDamageMetadata(entity.toUser(), projectile, force)
                )
            }

            is SpectralArrow -> {
                putProjectileDamageMetadata(
                    projectile.uniqueId,
                    PlayerArrowDamageMetadata(entity.toUser(), projectile, force)
                )
            }
        }
    }

    fun putProjectileDamageMetadata(uuid: UUID, projectileDamageMetadata: ProjectileDamageMetadata) {
        projectileDamageMetadataMap.put(uuid, projectileDamageMetadata)
    }

    fun findProjectileDamageMetadata(uuid: UUID): ProjectileDamageMetadata? {
        return projectileDamageMetadataMap.getIfPresent(uuid)
    }

    fun removeProjectileDamageMetadata(uuid: UUID) {
        projectileDamageMetadataMap.invalidate(uuid)
    }

    private val customDamageMetadataMap = Caffeine.newBuilder()
        .softValues()
        .expireAfterAccess(Duration.ofSeconds(4))
        .build<UUID, CustomDamageMetadata>()

    fun putCustomDamageMetadata(uuid: UUID, customDamageMetadata: CustomDamageMetadata) {
        customDamageMetadataMap.put(uuid, customDamageMetadata)
    }

    fun findCustomDamageMetadata(uuid: UUID): CustomDamageMetadata? {
        return customDamageMetadataMap.getIfPresent(uuid)
    }

    fun removeCustomDamageMetadata(uuid: UUID) {
        customDamageMetadataMap.invalidate(uuid)
    }

    private val cancelKnockbackSet: MutableSet<UUID> = mutableSetOf()

    fun markCancelKnockback(uuid: UUID) {
        cancelKnockbackSet.add(uuid)
    }

    fun unmarkCancelKnockback(uuid: UUID): Boolean {
        return cancelKnockbackSet.remove(uuid)
    }
    //TODO 更通用的临时标记工具类
}

/**
 * 对该实体造成萌芽伤害.
 */
fun LivingEntity.hurt(customDamageMetadata: CustomDamageMetadata, originEntity: LivingEntity?) {
    val defenseMetadata = when (
        val damagee = this
    ) {
        is Player -> {
            EntityDefenseMetadata(damagee.toUser().attributeMap)
        }

        else -> {
            EntityDefenseMetadata(EntityAttributeAccessor.getAttributeMap(damagee))
        }
    }
    val finalDamage = defenseMetadata.calculateFinalDamage(customDamageMetadata)
    DamageManager.putCustomDamageMetadata(this.uniqueId, customDamageMetadata)
    this.damage(finalDamage, originEntity)
}

/**
 * 伤害系统中与公式有关的内容
 */
object DamageRules {
    /**
     * 计算*单种元素*被防御后的伤害.
     *
     * 影响防御后伤害的因素:
     * - 原始伤害值
     * - 防御(本元素+通用元素)
     * - 防御穿透
     */
    fun calculateDamageAfterDefense(
        originalDamage: Double, defense: Double, defensePenetration: Double, defensePenetrationRate: Double,
    ): Double {
        val validDefense = (defense - defensePenetration).coerceAtLeast(0.0) * (1 - defensePenetrationRate)
        return (originalDamage - validDefense).coerceAtLeast(0.0)
    }

    /**
     * 通过拉弓的时间计算拉弓的力度.
     */
    fun calculateBowForce(useTicks: Int): Float {
        val useSeconds = useTicks / 20F
        val force = (useSeconds * useSeconds + useSeconds * 2F) / 3F
        return force.coerceIn(0F, 1F)
    }
}
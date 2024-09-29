package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.attack.*
import cc.mewcraft.wakame.attribute.EntityAttributeAccessor
import cc.mewcraft.wakame.item.components.*
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.user.toUser
import com.github.benmanes.caffeine.cache.Caffeine
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import java.time.Duration
import java.util.UUID

object DamageManager {
    fun generateDamageMetadata(event: EntityDamageEvent): DamageMetadata? {
        // 先检查是不是自定义伤害; 若是, 则直接返回自定义伤害信息
        val uuid = event.entity.uniqueId
        val customDamageMetadata = findCustomDamageMetadata(uuid)
        if (customDamageMetadata != null) {
            // 如果自定义伤害有源且需要取消击退
            // FIXED BUG: 无源伤害(即没有造成伤害的LivingEntity)不会触发击退事件
            if (!customDamageMetadata.knockback && (event is EntityDamageByEntityEvent)) {
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
                    val attack = itemStack.tryNekoStack?.templates?.get(ItemTemplateTypes.ATTACK)
                    if (attack != null) {
                        when (event.cause) {
                            DamageCause.ENTITY_ATTACK -> {
                                when (attack.attackType) {
                                    is SwordAttack -> {
                                        return PlayerMeleeAttackMetadata(damager.toUser(), DamageTags(DamageTag.MELEE, DamageTag.SWORD))
                                    }

                                    is AxeAttack -> {
                                        return PlayerMeleeAttackMetadata(damager.toUser(), DamageTags(DamageTag.MELEE, DamageTag.AXE))
                                    }

                                    is TridentAttack -> {
                                        return PlayerMeleeAttackMetadata(damager.toUser(), DamageTags(DamageTag.MELEE, DamageTag.TRIDENT))
                                    }

                                    is MaceAttack -> {
                                        return PlayerMeleeAttackMetadata(damager.toUser(), DamageTags(DamageTag.MELEE, DamageTag.MACE))
                                    }

                                    is HammerAttack -> {
                                        return PlayerMeleeAttackMetadata(damager.toUser(), DamageTags(DamageTag.MELEE, DamageTag.HAMMER))
                                    }

                                    else -> {
                                        // 玩家使用非直接攻击实体的攻击特效的物品
                                        // 如：使用弓、弩左键点击造成伤害
                                        // 留到最后视为原版伤害处理
                                    }
                                }
                            }

                            DamageCause.ENTITY_SWEEP_ATTACK -> {
                                // 需要Attack物品的攻击特效是“sword”才能打出横扫伤害
                                if (attack.attackType is SwordAttack) {
                                    return PlayerMeleeAttackMetadata(damager.toUser(), DamageTags(DamageTag.MELEE, DamageTag.SWORD, DamageTag.EXTRA))
                                } else {
                                    // 取消Bukkit伤害事件
                                    event.isCancelled = true
                                    // 返回null是为了供外部识别以不触发neko伤害事件
                                    return null
                                }
                            }

                            else -> {
                                // 其他Cause的玩家伤害
                                // 留到最后视为原版伤害处理
                                // 理论上来说只有玩家丢伤害药水进入这个else
                                // 或使用非常规手段使玩家造成异常类型的伤害
                            }
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

                    if (damager is AbstractArrow) {
                        return DefaultProjectileDamageMetadata(damager)
                    }
                }
            }
        }

        /*
        如果:
         - 伤害没有攻击者
         - 玩家使用无Attack组件物品、非直接攻击实体的攻击特效的物品（弓等）、非neko物品攻击
         - 伤害攻击者非玩家、living实体、弹射物三者之一
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
                EntityDefenseMetadata(EntityAttributeMapAccess.get(damagee).getOrElse {
                    error("Failed to generate defense metadata because the entity does not have an attribute map.")
                })
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
                            PlayerTridentDamageMetadata(shooter.toUser(), projectile, DamageTags(DamageTag.PROJECTILE, DamageTag.TRIDENT))
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

        val damageTags = DamageTags(DamageTag.PROJECTILE)
        val bowMaterial = event.bow?.type
        if (bowMaterial == Material.BOW) {
            damageTags.add(DamageTag.BOW)
        } else if (bowMaterial == Material.CROSSBOW) {
            damageTags.add(DamageTag.CROSSBOW)
        }

        when (projectile) {
            is Arrow -> {
                putProjectileDamageMetadata(
                    projectile.uniqueId,
                    PlayerArrowDamageMetadata(entity.toUser(), projectile, force, damageTags)
                )
            }

            is SpectralArrow -> {
                putProjectileDamageMetadata(
                    projectile.uniqueId,
                    PlayerArrowDamageMetadata(entity.toUser(), projectile, force, damageTags)
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
    // TODO 更通用的临时标记工具类
}

/**
 * 对该实体造成萌芽伤害.
 */
fun LivingEntity.hurt(customDamageMetadata: CustomDamageMetadata, source: LivingEntity?) {
    val defenseMetadata = when (
        val damagee = this
    ) {
        is Player -> {
            EntityDefenseMetadata(damagee.toUser().attributeMap)
        }

        else -> {
            EntityDefenseMetadata(EntityAttributeMapAccess.get(damagee).getOrElse {
                error("Failed to hurt the living entity because the entity does not have an attribute map.")
            })
        }
    }
    val finalDamage = customDamageMetadata.damageBundle.packets().sumOf {
        defenseMetadata.calculateFinalDamage(it.element, customDamageMetadata)
    }
    DamageManager.putCustomDamageMetadata(this.uniqueId, customDamageMetadata)
    this.damage(finalDamage, source)
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

package cc.mewcraft.wakame.world.attribute.damage

import cc.mewcraft.wakame.attribute.EntityAttributeAccessor
import cc.mewcraft.wakame.item.behavior.ItemBehaviorTypes
import cc.mewcraft.wakame.item.tryNekoStack
import cc.mewcraft.wakame.user.toUser
import com.github.benmanes.caffeine.cache.Caffeine
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.Trident
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.projectiles.BlockProjectileSource
import java.time.Duration
import java.util.UUID

object DamageManager {
    fun generateDamageMetaData(event: EntityDamageEvent): DamageMetadata {
        // 先检查是不是自定义伤害
        // 若是，则直接返回自定义伤害信息
        val uuid = event.entity.uniqueId
        val customDamageMetaData = findCustomDamageMetaData(uuid)
        if (customDamageMetaData != null) {
            return customDamageMetaData
        }

        // 不是自定义伤害
        // 存在造成伤害的实体时
        if (event is EntityDamageByEntityEvent) {
            when (val damager = event.damager) {
                // 造成伤害的是玩家
                is Player -> {
                    val itemStack = damager.inventory.itemInMainHand
                    // 玩家手中的物品是Attack
                    if (itemStack.tryNekoStack?.behaviors?.has(ItemBehaviorTypes.ATTACK) == true) {
                        return when (event.cause) {
                            DamageCause.ENTITY_ATTACK -> {
                                PlayerMeleeAttackMetadata(damager.toUser(), false)
                            }

                            DamageCause.ENTITY_SWEEP_ATTACK -> {
                                PlayerMeleeAttackMetadata(damager.toUser(), true)
                            }

                            /**
                             * 纯原版的情况下，理论上玩家能够产生的伤害类型只有：
                             * ENTITY_ATTACK
                             * ENTITY_SWEEP_ATTACK
                             * MAGIC
                             * 其他伤害类型均视为原版伤害
                             */
                            else -> {
                                DefaultDamageMetadata(event.damage)
                            }
                        }
                        // 玩家手中的物品不是Attack，甚至不是Neko
                    } else {
                        return DefaultDamageMetadata(event.damage)
                    }
                }

                // 造成伤害的是非玩家实体
                is LivingEntity -> {
                    return EntityMeleeAttackMetadata(damager)
                }

                // 造成伤害的是弹射物
                // 查找是否存在记录，若不存在，按照默认伤害处理
                is Projectile -> {
                    return findProjectileDamageMetaData(damager.uniqueId) ?: DefaultDamageMetadata(event.damage)
                }
            }
            // 如果造成伤害的实体不是LivingEntity也不是弹射物
            // 这种情况理论上不应该存在，若出现，按照默认伤害处理
            return DefaultDamageMetadata(event.damage)
        }
        // 如果伤害没有攻击者，视为原版伤害
        // TODO 对一些原版伤害进行修饰，比如加强溺水伤害
        return DefaultDamageMetadata(event.damage)
    }

    fun generateDefenseMetaData(event: EntityDamageEvent): DefenseMetadata {
        return when (val damagee = event.entity) {
            is Player -> {
                EntityDefenseMetadata(damagee.toUser().attributeMap)
            }

            is LivingEntity -> {
                EntityDefenseMetadata(EntityAttributeAccessor.getAttributeMap(damagee))
            }

            else -> {
                throw IllegalArgumentException("damagee is not living entity!")
            }
        }
    }

    /**
     * 弹射物生成时，其伤害信息就应该确定了
     * 只记录需要wakame属性系统处理的伤害信息
     * 伤害信息不存在时，弹射物伤害将按原版处理
     * 伤害信息过期的情况如下：
     * 超过有效期（60秒）
     * 弹射物击中方块
     */
    private val projectileDamageMetaDataMap = Caffeine.newBuilder().softValues().expireAfterAccess(Duration.ofSeconds(60)).build<UUID, ProjectileDamageMetadata>()

    fun recordProjectileDamageMetaData(event: ProjectileLaunchEvent) {
        when (val projectile = event.entity) {
            // 弹射物是三叉戟
            is Trident -> {
                when (val shooter = projectile.shooter) {
                    is Player -> {
                        putProjectileDamageMetaData(
                            projectile.uniqueId,
                            PlayerProjectileDamageMetadata(ProjectileType.TRIDENT, shooter.toUser(), projectile.itemStack, 1F)
                        )
                    }

                    is LivingEntity -> {
                        putProjectileDamageMetaData(
                            projectile.uniqueId,
                            EntityProjectileDamageMetadata(ProjectileType.TRIDENT, shooter)
                        )
                    }
                }
            }

            // 弹射物是箭矢（普通箭、光灵箭、药水箭）
            is AbstractArrow -> {
                when (val shooter = projectile.shooter) {
                    is Player -> {
                        // 玩家发射箭矢时，由EntityShootBowEvent处理
                        // 原因是需要获取玩家拉弓的力度
                    }

                    is LivingEntity -> {
                        putProjectileDamageMetaData(
                            projectile.uniqueId,
                            EntityProjectileDamageMetadata(ProjectileType.ARROWS, shooter)
                        )
                    }

                    is BlockProjectileSource -> {
                        // TODO 让自定义箭矢在发射器中有效
                    }
                }
            }

            // TODO 可能还会有其他需要wakame属性系统处理的弹射物
        }
    }

    fun recordProjectileDamageMetaData(event: EntityShootBowEvent) {
        val entity = event.entity
        if (entity !is Player) return

        val projectile = event.projectile
        if (projectile !is AbstractArrow) return
        if (projectile is Trident) return

        // 玩家射出的箭矢伤害需要根据拉弓的力度进行调整
        val force = DamageRules.calculateBowForce(72000 - entity.itemUseRemainingTime)
        putProjectileDamageMetaData(
            projectile.uniqueId,
            PlayerProjectileDamageMetadata(ProjectileType.ARROWS, entity.toUser(), projectile.itemStack, force)
        )
    }

    fun putProjectileDamageMetaData(uuid: UUID, projectileDamageMetaData: ProjectileDamageMetadata) {
        projectileDamageMetaDataMap.put(uuid, projectileDamageMetaData)
    }

    fun findProjectileDamageMetaData(uuid: UUID): ProjectileDamageMetadata? {
        return projectileDamageMetaDataMap.getIfPresent(uuid)
    }

    fun removeProjectileDamageMetaData(uuid: UUID) {
        projectileDamageMetaDataMap.invalidate(uuid)
    }

    private val customDamageMetaDataMap = Caffeine.newBuilder().softValues().expireAfterAccess(Duration.ofSeconds(60)).build<UUID, CustomDamageMetadata>()

    fun putCustomDamageMetaData(uuid: UUID, customDamageMetaData: CustomDamageMetadata) {
        customDamageMetaDataMap.put(uuid, customDamageMetaData)
    }

    fun findCustomDamageMetaData(uuid: UUID): CustomDamageMetadata? {
        return customDamageMetaDataMap.getIfPresent(uuid)
    }

    fun removeCustomDamageMetaData(uuid: UUID) {
        customDamageMetaDataMap.invalidate(uuid)
    }
}

fun LivingEntity.applyCustomDamage(customDamageMetaData: CustomDamageMetadata, originEntity: LivingEntity?) {
    val defenseMetaData = when (val damagee = this) {
        is Player -> {
            EntityDefenseMetadata(damagee.toUser().attributeMap)
        }

        else -> {
            EntityDefenseMetadata(EntityAttributeAccessor.getAttributeMap(damagee))
        }
    }
    val finalDamage = defenseMetaData.calculateFinalDamage(customDamageMetaData)
    DamageManager.putCustomDamageMetaData(this.uniqueId, customDamageMetaData)
    this.damage(finalDamage, originEntity)
}

/**
 * 伤害系统中与公式有关的内容
 */
object DamageRules {
    /**
     * 计算 单种元素 被防御后的伤害
     * 影响防御后伤害的因素：原始伤害值、防御（本元素+通用元素）、防御穿透
     * TODO 添加从配置文件载入防御计算公式的功能
     */
    fun calculateDamageAfterDefense(
        originalDamage: Double, defense: Double, defensePenetration: Double,
    ): Double {
        val validDefense = (defense - defensePenetration).coerceAtLeast(0.0)
        return (originalDamage - validDefense).coerceAtLeast(0.0)
    }

    /**
     * 通过拉弓的时间计算拉弓的力度
     * TODO 添加从配置文件载入防御计算公式的功能
     */
    fun calculateBowForce(useTicks: Int): Float {
        val useSeconds = useTicks / 20F
        val force = (useSeconds * useSeconds + useSeconds * 2F) / 3F
        return force.coerceIn(0F, 1F)
    }
}
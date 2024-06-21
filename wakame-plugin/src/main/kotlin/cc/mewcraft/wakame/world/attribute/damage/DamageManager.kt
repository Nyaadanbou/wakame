package cc.mewcraft.wakame.world.attribute.damage

import cc.mewcraft.wakame.item.binary.playNekoStack
import cc.mewcraft.wakame.item.hasBehavior
import cc.mewcraft.wakame.item.schema.behavior.Attack
import cc.mewcraft.wakame.user.toUser
import org.bukkit.entity.*
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.projectiles.BlockProjectileSource
import java.util.UUID

object DamageManager {
    fun generateDamageMetaData(event: EntityDamageEvent): DamageMetaData {
        //存在造成伤害的实体时
        if (event is EntityDamageByEntityEvent) {
            when (val damager = event.damager) {
                //造成伤害的是玩家
                is Player -> {
                    val nekoStack = damager.inventory.itemInMainHand.playNekoStack
                    //玩家手中的物品是Attack
                    if (nekoStack.isNeko && nekoStack.hasBehavior<Attack>()) {
                        return when (event.cause) {
                            DamageCause.ENTITY_ATTACK -> {
                                PlayerMeleeAttackMetaData(damager.toUser(), false)
                            }

                            DamageCause.ENTITY_SWEEP_ATTACK -> {
                                PlayerMeleeAttackMetaData(damager.toUser(), true)
                            }

                            /**
                             * 纯原版的情况下，理论上玩家能够产生的伤害类型只有：
                             * ENTITY_ATTACK
                             * ENTITY_SWEEP_ATTACK
                             * MAGIC
                             * 其他伤害类型均视为原版伤害
                             */
                            else -> {
                                DefaultDamageMetaData(event.damage)
                            }
                        }
                        //玩家手中的物品不是Attack，甚至不是Neko
                    } else {
                        return DefaultDamageMetaData(event.damage)
                    }
                }

                //造成伤害的是非玩家实体
                is LivingEntity -> {
                    return EntityMeleeAttackMetaData(damager)
                }

                //造成伤害的是弹射物
                //查找是否存在记录，若不存在，按照默认伤害处理
                is Projectile -> {
                    return findProjectileDamageMetaData(damager.uniqueId) ?: DefaultDamageMetaData(event.damage)
                }
            }
            //如果造成伤害的实体不是LivingEntity也不是弹射物
            //这种情况理论上不应该存在，若出现，按照默认伤害处理
            return DefaultDamageMetaData(event.damage)
        }
        //如果伤害没有攻击者，视为原版伤害
        //TODO 对一些原版伤害进行修饰，比如加强溺水伤害
        return DefaultDamageMetaData(event.damage)
    }

    fun generateDefenseMetaData(event: EntityDamageEvent): DefenseMetaData {
        TODO("获得受伤实体的AttributeMap")
    }

    private val projectileDamageMetaDataMap = mutableMapOf<UUID, ProjectileDamageMetaData>()


    /**
     * 弹射物生成时，其伤害信息就应该确定了
     * 只记录需要wakame属性系统处理的伤害信息
     */
    fun recordProjectileDamageMetaData(event: ProjectileLaunchEvent) {
        when (val projectile = event.entity) {
            //弹射物是三叉戟
            is Trident -> {
                when (val shooter = projectile.shooter) {
                    is Player -> {
                        projectileDamageMetaDataMap.put(
                            projectile.uniqueId,
                            PlayerProjectileDamageMetaData(ProjectileType.TRIDENT, shooter.toUser(), projectile.itemStack)
                        )
                    }

                    is LivingEntity -> {
                        TODO()
                    }
                }
            }

            //弹射物是箭矢（普通箭、光灵箭、药水箭）
            is AbstractArrow -> {
                when (val shooter = projectile.shooter) {
                    is Player -> {
                        projectileDamageMetaDataMap.put(
                            projectile.uniqueId,
                            PlayerProjectileDamageMetaData(ProjectileType.ARROWS, shooter.toUser(), projectile.itemStack)
                        )
                    }

                    is LivingEntity -> {
                        TODO()
                    }

                    is BlockProjectileSource -> {
                        TODO()
                    }
                }
            }

            //可能还会有其他需要wakame属性系统处理的弹射物
        }
    }

    fun findProjectileDamageMetaData(uuid: UUID): ProjectileDamageMetaData? {
        return projectileDamageMetaDataMap.get(uuid)
    }

    fun removeProjectileDamageMetaData(uuid: UUID) {
        projectileDamageMetaDataMap.remove(uuid)
    }
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
        return originalDamage * (1 - (validDefense / 1 * originalDamage + validDefense))
    }
}
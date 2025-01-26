@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.attack.SwordAttack
import cc.mewcraft.wakame.attribute.AttributeMapAccess
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.attribute.ImaginaryAttributeMaps
import cc.mewcraft.wakame.damage.mappings.DamageTypeMappings
import cc.mewcraft.wakame.damage.mappings.DirectEntityTypeMappings
import cc.mewcraft.wakame.damage.mappings.EntityAttackMappings
import cc.mewcraft.wakame.item.ItemSlot
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.shadowNeko
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.user.toUser
import com.github.benmanes.caffeine.cache.Caffeine
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import java.time.Duration
import java.util.*

/**
 * @see DamageManagerApi.hurt
 */
fun LivingEntity.hurt(damageMetadata: DamageMetadata, source: LivingEntity? = null, knockback: Boolean = false) {
    DamageManager.hurt(this, damageMetadata, source, knockback)
}

/**
 * 包含伤害系统的核心逻辑和状态.
 */
object DamageManager : DamageManagerApi {

    /**
     * 对 [victim] 造成由 [damageMetadata] 指定的萌芽伤害.
     * 当 [source] 为 `null` 时, 伤害属于无源, 即不会产生击退.
     */
    override fun hurt(victim: LivingEntity, damageMetadata: DamageMetadata, source: LivingEntity?, knockback: Boolean) {
        putCustomDamageMetadata(victim.uniqueId, damageMetadata)

        // 如果自定义伤害有源且需要取消击退.
        // 这里修复了: 无源伤害(即没有造成伤害的 LivingEntity)不会触发击退事件.
        if (!knockback && source != null) {
            markCancelKnockback(victim.uniqueId)
        }

        // 造成伤害
        DamageApplier.damage(victim, source, 4.95)
    }

    /**
     * 能够造成伤害的弹射物类型.
     * 伤害系统会处理这些弹射物.
     */
    private val DAMAGE_PROJECTILE_TYPES: List<EntityType> = listOf(
        EntityType.ARROW,
        EntityType.BREEZE_WIND_CHARGE,
//        EntityType.DRAGON_FIREBALL,
        EntityType.EGG,
        EntityType.ENDER_PEARL,
        EntityType.FIREBALL,
        EntityType.FIREWORK_ROCKET,
//        EntityType.FISHING_BOBBER,
        EntityType.LLAMA_SPIT,
        EntityType.POTION,
        EntityType.SHULKER_BULLET,
        EntityType.SMALL_FIREBALL,
        EntityType.SNOWBALL,
        EntityType.SPECTRAL_ARROW,
        EntityType.TRIDENT,
        EntityType.WIND_CHARGE,
    )

    /**
     * 作为直接实体时, 能够造成伤害的实体类型.
     * 伤害系统会处理这些实体.
     */
    private val DAMAGE_DIRECT_ENTITY_TYPES = DAMAGE_PROJECTILE_TYPES + listOf(
        EntityType.TNT,
        EntityType.END_CRYSTAL,
        EntityType.AREA_EFFECT_CLOUD,
    )

    /**
     * 需要记录的弹射物.
     * 伤害系统会在这些弹射物生成时就记录伤害元数据.
     */
    private val MARK_PROJECTILE_TYPES: List<EntityType> = listOf(
        EntityType.ARROW,
        EntityType.SPECTRAL_ARROW,
        EntityType.TRIDENT,
    )


    fun generateDamageMetadata(event: EntityDamageEvent): DamageMetadata? {
        // 先检查是不是自定义伤害; 若是, 则直接返回自定义伤害信息
        val uuid = event.entity.uniqueId
        val customDamageMetadata = findCustomDamageMetadata(uuid)
        if (customDamageMetadata != null) {
            removeCustomDamageMetadata(uuid)
            return customDamageMetadata
        }

        val damageSource = event.damageSource
        val causingEntity = damageSource.causingEntity
        val directEntity = damageSource.directEntity

        // 不存在直接实体
        // 自然伤害(溺水、岩浆)
        // 使用伤害类型映射, 根据伤害类型调整伤害
        if (directEntity == null) {
            val mapping = DamageTypeMappings.get(damageSource.damageType)
            return mapping.builder.build(event)
        }

        // 存在直接实体后
        when (causingEntity) {
            // 无来源实体
            null -> {
                if (directEntity.type in DAMAGE_DIRECT_ENTITY_TYPES) {
                    // 使用默认
                    return buildDirectEntityDamageMetadataByDefault(directEntity, event, false)
                } else {
                    // 不应该发生
                    return warnAndDefaultReturn(event)
                }
            }
            // 来源实体是玩家
            is Player -> {
                // 直接实体是需要记录弹射物
                // 查找弹射物是否被记录, 尝试返回记录的值
                // 箭矢和三叉戟的伤害和词条栏有关, 会被记录
                // 没有记录则使用默认
                if (directEntity.type in MARK_PROJECTILE_TYPES) {
                    return findProjectileDamageMetadata(directEntity.uniqueId) ?: buildDirectEntityDamageMetadataByDefault(directEntity, event, true)
                } else if (directEntity.type in DAMAGE_DIRECT_ENTITY_TYPES) {
                    return buildDirectEntityDamageMetadataByDefault(directEntity, event, true)
                }

                val itemStack = causingEntity.inventory.itemInMainHand
                val nekoStack = itemStack.shadowNeko(false)
                val attack = nekoStack?.templates?.get(ItemTemplateTypes.ATTACK)
                when (event.cause) {
                    DamageCause.ENTITY_ATTACK -> {
                        if (attack != null) {
                            // 玩家手中的物品是 Attack
                            // 可能返回null, 意为取消伤害事件
                            return attack.attackType.generateDamageMetadata(causingEntity, nekoStack)
                        } else {
                            // 手中的物品无 Attack 行为甚至不是 NekoStack
                            return PlayerDamageMetadata.HAND_WITHOUT_ATTACK
                        }
                    }

                    DamageCause.ENTITY_SWEEP_ATTACK -> {
                        // TODO 横扫的临时实现, 等待麻将的组件化
                        // 需要玩家手中的物品是 Attack 且攻击特效是 “sword” 才能打出横扫伤害
                        if (attack?.attackType is SwordAttack) {
                            val attributeMap = causingEntity.toUser().attributeMap
                            return PlayerDamageMetadata(
                                user = causingEntity.toUser(),
                                damageTags = DamageTags(DamageTag.MELEE, DamageTag.SWORD),
                                damageBundle = damageBundle(attributeMap) {
                                    every {
                                        standard()
                                        rate { standard() * attributeMap.getValue(Attributes.SWEEPING_DAMAGE_RATIO) }
                                    }
                                }
                            )
                        } else {
                            // 返回 null 是为了供外部识别以不触发萌芽伤害事件
                            return null
                        }
                    }

                    else -> {
                        // 其他 Cause 的玩家伤害
                        // 不应该发生
                        return warnAndDefaultReturn(event)
                    }
                }
            }
            // 来源实体是非玩家生物
            is LivingEntity -> {
                // 直接实体是TNT、末影水晶, 视为无源处理
                if (directEntity.type == EntityType.TNT || directEntity.type == EntityType.END_CRYSTAL) {
                    return buildDirectEntityDamageMetadataByDefault(directEntity, event, false)
                }
                val mapping = EntityAttackMappings.getForVanilla(causingEntity, event)
                if (mapping == null) {
                    // 配置文件未指定该情景下生物的伤害映射
                    // 返回默认元素、无防御穿透、无暴击、原版伤害值
                    LOGGER.warn("The vanilla entity damage from '${damageSource.causingEntity?.type}' to '${event.entity.type}' by '${damageSource.directEntity?.type}' with damage type of '${damageSource.damageType.key}' is not config! Use default damage metadata.")
                    return EntityDamageMetadata(
                        damageBundle = damageBundle {
                            default {
                                min(event.damage)
                                max(event.damage)
                                rate(1.0)
                                defensePenetration(0.0)
                                defensePenetrationRate(0.0)
                            }
                        },
                        criticalStrikeMetadata = CriticalStrikeMetadata.NONE
                    )
                } else {
                    // 配置文件指定了该情景下生物的伤害映射
                    return mapping.generateDamageMetadata(event)
                }
            }
            // 不可能发生
            else -> {
                return warnAndDefaultReturn(event)
            }
        }
    }

    private fun buildArrowDamageBundleByCells(arrow: AbstractArrow): DamageBundle? {
        val itemStack = arrow.itemStack
        val nekoStack = itemStack.shadowNeko(false) ?: return null
        if (!nekoStack.templates.has(ItemTemplateTypes.ARROW)) {
            return null
        }
        val cells = nekoStack.components.get(ItemComponentTypes.CELLS) ?: return null
        val attributeModifiers = cells.collectAttributeModifiers(nekoStack, ItemSlot.imaginary())
        val arrowImaginaryAttributeMap by ImaginaryAttributeMaps.ARROW
        val attributeMapSnapshot = arrowImaginaryAttributeMap.getSnapshot()
        attributeModifiers.forEach { attribute, modifier ->
            attributeMapSnapshot.getInstance(attribute)?.addModifier(modifier)
        }
        return damageBundle(attributeMapSnapshot) { every { standard() } }
    }

    private val ARROW_TYPES: List<EntityType> = listOf(EntityType.ARROW, EntityType.SPECTRAL_ARROW)

    /**
     * 无来源实体时直接伤害实体的伤害元数据
     * 使用直接伤害实体类型映射, 根据弹射物类型获取伤害元数据
     * 箭矢特殊处理, 计算词条栏伤害
     */
    private fun buildDirectEntityDamageMetadataByDefault(directEntity: Entity, event: EntityDamageEvent, forPlayer: Boolean): DamageMetadata {
        val entityType = directEntity.type
        if (entityType in ARROW_TYPES) {
            val damageBundle = buildArrowDamageBundleByCells(directEntity as AbstractArrow)
            if (damageBundle != null) {
                return VanillaDamageMetadata(damageBundle)
            }
        }
        val damageMapping = if (forPlayer) {
            DirectEntityTypeMappings.getForPlayer(entityType, event)
        } else {
            DirectEntityTypeMappings.getForNoCause(entityType, event)
        }
        if (damageMapping == null) {
            LOGGER.warn("The damage from 'null' to '${event.entity.type}' by '${entityType}' with damage type of '${event.damageSource.damageType.key}' is not config ! Use default damage metadata.")
            return VanillaDamageMetadata(event.damage)
        }
        return damageMapping.builder.build(event)
    }

    private fun warnAndDefaultReturn(event: EntityDamageEvent): DamageMetadata {
        val damageSource = event.damageSource
        LOGGER.warn("Why can ${damageSource.causingEntity?.type} cause damage to ${event.entity.type} through ${damageSource.directEntity?.type}? This should not happen.")
        return VanillaDamageMetadata(event.damage)
    }

    fun generateDefenseMetadata(event: EntityDamageEvent): DefenseMetadata {
        return when (
            val damagee = event.entity
        ) {
            is Player -> {
                EntityDefenseMetadata(damagee.toUser().attributeMap)
            }

            is LivingEntity -> {
                EntityDefenseMetadata(AttributeMapAccess.get(damagee).getOrElse {
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
        .build<UUID, DamageMetadata>()

    /**
     * 在弹射物射出时记录其 [DamageMetadata].
     * 目前只记录玩家的箭矢和三叉戟.
     *
     * 伤害元数据过期的情况如下 (满足其一):
     * - 超过有效期 (60秒)
     * - 击中方块
     */
    fun recordProjectileDamageMetadata(event: ProjectileLaunchEvent) {
        val projectile = event.entity

        val shooter = projectile.shooter
        if (shooter !is Player) return
        when (projectile) {
            is Trident -> {
                putProjectileDamageMetadata(
                    projectile.uniqueId,
                    PlayerDamageMetadata(
                        user = shooter.toUser(),
                        damageTags = DamageTags(DamageTag.PROJECTILE, DamageTag.TRIDENT),
                        damageBundle = damageBundle(shooter.toUser().attributeMap) { every { standard() } }
                    )
                )
            }

            is Arrow, is SpectralArrow -> {
                // 玩家箭矢的伤害信息需要拉弓力度
                // 由 #recordProjectileDamageMetadata(event: EntityShootBowEvent) 处理
            }
        }
    }

    /**
     * 在弹射物射出时记录其 [DamageMetadata].
     * 目前只记录玩家的箭矢和三叉戟.
     * 玩家射出的箭矢伤害需要根据拉弓的力度进行调整.
     * 故需要监听此事件.
     *
     * 伤害元数据过期的情况如下 (满足其一):
     * - 超过有效期 (60秒)
     * - 击中方块
     */
    fun recordProjectileDamageMetadata(event: EntityShootBowEvent) {
        val entity = event.entity
        if (entity !is Player) return

        val projectile = event.projectile
        if (projectile !is AbstractArrow) return

        val force: Float
        val damageTags: DamageTags
        val damageBundle: DamageBundle
        val bowMaterial = event.bow?.type
        when (bowMaterial) {
            Material.BOW -> {
                damageTags = DamageTags(DamageTag.PROJECTILE, DamageTag.BOW)
                // 玩家射出的箭矢伤害需要根据拉弓的力度进行调整
                force = DamageRules.calculateBowForce(72000 - entity.itemUseRemainingTime)
            }

            Material.CROSSBOW -> {
                damageTags = DamageTags(DamageTag.PROJECTILE, DamageTag.CROSSBOW)
                force = 1F
            }

            else -> {
                return
            }
        }


        val itemStack = projectile.itemStack
        val nekoStack = itemStack.shadowNeko(false)
        val cells = nekoStack?.components?.get(ItemComponentTypes.CELLS)
        if (nekoStack?.templates?.has(ItemTemplateTypes.ARROW) == true && cells != null) {
            val attributeModifiers = cells.collectAttributeModifiers(nekoStack, ItemSlot.imaginary())
            val attributeMapSnapshot = entity.toUser().attributeMap.getSnapshot()
            attributeModifiers.forEach { attribute, modifier ->
                attributeMapSnapshot.getInstance(attribute)?.addModifier(modifier)
            }

            damageBundle = damageBundle(attributeMapSnapshot) {
                every {
                    standard()
                    min { force * standard() }
                    max { force * standard() }
                }
            }
        } else {
            damageBundle = damageBundle(entity.toUser().attributeMap) {
                every {
                    standard()
                    min { force * standard() }
                    max { force * standard() }
                }
            }
        }

        putProjectileDamageMetadata(
            projectile.uniqueId,
            PlayerDamageMetadata(
                user = entity.toUser(),
                damageTags = damageTags,
                damageBundle = damageBundle
            )
        )
    }

    fun putProjectileDamageMetadata(uuid: UUID, damageMetadata: DamageMetadata) {
        projectileDamageMetadataMap.put(uuid, damageMetadata)
    }

    fun findProjectileDamageMetadata(uuid: UUID): DamageMetadata? {
        return projectileDamageMetadataMap.getIfPresent(uuid)
    }

    fun removeProjectileDamageMetadata(uuid: UUID) {
        projectileDamageMetadataMap.invalidate(uuid)
    }

    private val customDamageMetadataMap = Caffeine.newBuilder()
        .softValues()
        .expireAfterAccess(Duration.ofSeconds(4))
        .build<UUID, DamageMetadata>()

    fun putCustomDamageMetadata(uuid: UUID, damageMetadata: DamageMetadata) {
        customDamageMetadataMap.put(uuid, damageMetadata)
    }

    fun findCustomDamageMetadata(uuid: UUID): DamageMetadata? {
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
